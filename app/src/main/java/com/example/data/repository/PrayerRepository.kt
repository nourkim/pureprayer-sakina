package com.example.data.repository

import com.example.data.local.PrayerDao
import com.example.data.local.PrayerDayEntity
import com.example.data.local.SettingsPreferences
import com.example.data.location.DeviceLocationHelper
import com.example.data.location.LocationZoneCatalog
import com.example.data.model.CityZone
import com.example.data.model.LocationMode
import com.example.data.remote.AladhanApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class PrayerRepository(
    private val prayerDao: PrayerDao,
    private val prefs: SettingsPreferences,
    private val apiService: AladhanApiService,
    private val locationHelper: DeviceLocationHelper
) {

    fun observeToday(timezoneId: String): Flow<PrayerDayEntity?> {
        val todayIso = todayIsoForTimezone(timezoneId)
        return prayerDao.observeDay(todayIso)
    }

    suspend fun getDaySync(dateIso: String): PrayerDayEntity? =
        prayerDao.getDaySync(dateIso)

    suspend fun getUpcomingDaysSync(fromDateIso: String, limit: Int = 7): List<PrayerDayEntity> =
        prayerDao.getUpcomingDaysSync(fromDateIso, limit)

    /**
     * Checks whether the device region/timezone changed or cache is missing/corrupted,
     * and only hits the internet when needed.
     */
    suspend fun ensureScheduleUpToDate(forceGpsRefresh: Boolean = false): Result<Boolean> =
        withContext(Dispatchers.IO) {
            val current = prefs.getSettingsSync()
            val deviceTz = TimeZone.getDefault().id

            var targetLat = current.latitude
            var targetLon = current.longitude
            var targetCity = current.cityName
            var targetTz = current.timezoneId
            var targetMethod = current.methodId
            var zoneChanged = false
            var useExplicitTimezoneInApi = (current.locationMode == LocationMode.MANUAL)

            if (current.locationMode == LocationMode.GPS) {
                val gpsLoc = if (locationHelper.hasLocationPermission() &&
                    (forceGpsRefresh || !current.hasSyncedOnline || current.timezoneId != deviceTz)
                ) {
                    locationHelper.getSingleLowPowerLocation()
                } else {
                    null
                }

                if (gpsLoc != null) {
                    useExplicitTimezoneInApi = false
                    val dist = LocationZoneCatalog.distanceKm(
                        current.latitude,
                        current.longitude,
                        gpsLoc.latitude,
                        gpsLoc.longitude
                    )
                    if (forceGpsRefresh || dist > 5.0 || gpsLoc.timezoneId != current.timezoneId || !current.hasSyncedOnline) {
                        zoneChanged = true
                        targetLat = gpsLoc.latitude
                        targetLon = gpsLoc.longitude
                        targetCity = gpsLoc.cityName
                        targetTz = gpsLoc.timezoneId
                        if (gpsLoc.timezoneId != current.timezoneId || !current.hasSyncedOnline) {
                            targetMethod = LocationZoneCatalog.recommendedMethodForTimezone(gpsLoc.timezoneId)
                        }
                        prefs.updateRegion(
                            latitude = targetLat,
                            longitude = targetLon,
                            cityName = targetCity,
                            timezoneId = targetTz,
                            methodId = targetMethod,
                            syncedOnline = false
                        )
                    }
                } else if (!current.hasSyncedOnline && current.timezoneId != deviceTz) {
                    // Only use generic timezone fallback city if we have NEVER acquired a GPS fix before
                    val fallbackCity = LocationZoneCatalog.defaultCityForTimezone(deviceTz)
                    zoneChanged = true
                    useExplicitTimezoneInApi = true
                    targetLat = fallbackCity.latitude
                    targetLon = fallbackCity.longitude
                    targetCity = fallbackCity.nameEn
                    targetTz = fallbackCity.timezoneId
                    targetMethod = fallbackCity.defaultMethodId
                    prefs.updateRegion(
                        latitude = targetLat,
                        longitude = targetLon,
                        cityName = targetCity,
                        timezoneId = targetTz,
                        methodId = targetMethod,
                        syncedOnline = false
                    )
                }
            }

            val todayIso = todayIsoForTimezone(targetTz)
            val cachedToday = prayerDao.getDaySync(todayIso)
            val hasMismatchedCache = cachedToday != null && (
                cachedToday.timezoneId != targetTz ||
                    cachedToday.methodId != targetMethod ||
                    cachedToday.fajr >= "12:00" ||
                    cachedToday.maghrib <= "12:00"
                )

            val needsFetch = zoneChanged ||
                cachedToday == null ||
                cachedToday.isEstimate ||
                hasMismatchedCache

            // Always ensure Room has an immediate valid local calculation if empty or mismatched
            if (cachedToday == null || hasMismatchedCache) {
                seedOfflineFallbackMonth(
                    lat = targetLat,
                    lon = targetLon,
                    tzId = targetTz,
                    cityLabel = targetCity,
                    methodId = targetMethod
                )
            }

            if (!needsFetch) {
                return@withContext Result.success(false)
            }

            return@withContext fetchAndCacheMonthFromNetwork(
                lat = targetLat,
                lon = targetLon,
                tzId = targetTz,
                cityLabel = targetCity,
                methodId = targetMethod,
                explicitTimezone = if (useExplicitTimezoneInApi) targetTz else null
            )
        }

    suspend fun selectManualCity(city: CityZone, methodOverride: Int? = null): Result<Boolean> =
        withContext(Dispatchers.IO) {
            val method = methodOverride ?: city.defaultMethodId
            prefs.setLocationMode(LocationMode.MANUAL)
            prefs.updateRegion(
                latitude = city.latitude,
                longitude = city.longitude,
                cityName = city.nameEn,
                timezoneId = city.timezoneId,
                methodId = method,
                manualCityId = city.id,
                syncedOnline = false
            )
            seedOfflineFallbackMonth(
                lat = city.latitude,
                lon = city.longitude,
                tzId = city.timezoneId,
                cityLabel = city.nameEn,
                methodId = method
            )
            fetchAndCacheMonthFromNetwork(
                lat = city.latitude,
                lon = city.longitude,
                tzId = city.timezoneId,
                cityLabel = city.nameEn,
                methodId = method,
                explicitTimezone = city.timezoneId
            )
        }

    suspend fun updateCalculationMethod(methodId: Int): Result<Boolean> =
        withContext(Dispatchers.IO) {
            val current = prefs.getSettingsSync()
            prefs.setMethodId(methodId)
            seedOfflineFallbackMonth(
                lat = current.latitude,
                lon = current.longitude,
                tzId = current.timezoneId,
                cityLabel = current.cityName,
                methodId = methodId
            )
            fetchAndCacheMonthFromNetwork(
                lat = current.latitude,
                lon = current.longitude,
                tzId = current.timezoneId,
                cityLabel = current.cityName,
                methodId = methodId,
                explicitTimezone = if (current.locationMode == LocationMode.MANUAL) current.timezoneId else null
            )
        }

    private suspend fun fetchAndCacheMonthFromNetwork(
        lat: Double,
        lon: Double,
        tzId: String,
        cityLabel: String,
        methodId: Int,
        explicitTimezone: String?
    ): Result<Boolean> {
        return try {
            val tz = TimeZone.getTimeZone(tzId)
            val cal = Calendar.getInstance(tz)
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1

            val entities = mutableListOf<PrayerDayEntity>()
            entities += fetchSingleMonth(year, month, lat, lon, tzId, cityLabel, methodId, explicitTimezone)

            if (cal.get(Calendar.DAY_OF_MONTH) >= 23) {
                val nextCal = (cal.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                runCatching {
                    entities += fetchSingleMonth(
                        nextCal.get(Calendar.YEAR),
                        nextCal.get(Calendar.MONTH) + 1,
                        lat,
                        lon,
                        tzId,
                        cityLabel,
                        methodId,
                        explicitTimezone
                    )
                }
            }

            if (entities.isNotEmpty()) {
                val resolvedTz = entities.first().timezoneId
                prayerDao.clearAll()
                prayerDao.insertDays(entities)
                prefs.markSyncedOnline(resolvedTimezoneId = resolvedTz)
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchSingleMonth(
        year: Int,
        month: Int,
        lat: Double,
        lon: Double,
        tzId: String,
        cityLabel: String,
        methodId: Int,
        explicitTimezone: String?
    ): List<PrayerDayEntity> {
        val response = apiService.getMonthlyCalendar(
            year = year,
            month = month,
            latitude = lat,
            longitude = lon,
            method = methodId,
            school = 0,
            timezoneString = explicitTimezone
        )
        val inFmt = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val outFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val now = System.currentTimeMillis()

        return response.data.mapNotNull { item ->
            val parsedDate = runCatching { inFmt.parse(item.date.gregorian.date) }.getOrNull()
                ?: return@mapNotNull null
            val iso = outFmt.format(parsedDate)
            PrayerDayEntity(
                dateIso = iso,
                fajr = cleanTime(item.timings.fajr),
                sunrise = cleanTime(item.timings.sunrise),
                dhuhr = cleanTime(item.timings.dhuhr),
                asr = cleanTime(item.timings.asr),
                maghrib = cleanTime(item.timings.maghrib),
                isha = cleanTime(item.timings.isha),
                hijriDay = item.date.hijri.day,
                hijriMonthEn = item.date.hijri.month.en,
                hijriMonthAr = item.date.hijri.month.ar,
                hijriYear = item.date.hijri.year,
                timezoneId = item.meta.timezone.ifBlank { tzId },
                latitude = lat,
                longitude = lon,
                locationLabel = cityLabel,
                methodId = methodId,
                isEstimate = false,
                fetchedAtMillis = now
            )
        }
    }

    private suspend fun seedOfflineFallbackMonth(
        lat: Double,
        lon: Double,
        tzId: String,
        cityLabel: String,
        methodId: Int
    ) {
        val tz = TimeZone.getTimeZone(tzId)
        val cal = Calendar.getInstance(tz)
        val list = mutableListOf<PrayerDayEntity>()
        for (offset in 0..35) {
            val dayCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }
            list += OfflinePrayerCalculator.computeDay(
                calendar = dayCal,
                latitude = lat,
                longitude = lon,
                timezoneId = tzId,
                locationLabel = cityLabel,
                methodId = methodId
            )
        }
        prayerDao.clearAll()
        prayerDao.insertDays(list)
    }

    private fun cleanTime(raw: String): String =
        raw.substringBefore(" ").trim().take(5)

    companion object {
        fun todayIsoForTimezone(timezoneId: String): String {
            val tz = TimeZone.getTimeZone(timezoneId)
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = tz }
            return fmt.format(Calendar.getInstance(tz).time)
        }
    }
}
