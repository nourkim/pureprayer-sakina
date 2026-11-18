package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.location.LocationZoneCatalog
import com.example.data.model.LocationMode
import com.example.data.model.SakinaLanguage
import com.example.data.model.SakinaThemeId
import com.example.data.model.SakinaToneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.TimeZone

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sakina_prefs")

data class UserSettings(
    val locationMode: LocationMode = LocationMode.GPS,
    val manualCityId: String = "makkah",
    val latitude: Double = 21.4225,
    val longitude: Double = 39.8262,
    val cityName: String = "Makkah",
    val timezoneId: String = "Asia/Riyadh",
    val methodId: Int = 4,
    val hasSyncedOnline: Boolean = false,
    val lastSyncMillis: Long = 0L,
    val language: SakinaLanguage = SakinaLanguage.SYSTEM,
    val themeId: SakinaThemeId = SakinaThemeId.RAWDAH_EMERALD,
    val toneId: SakinaToneId = SakinaToneId.NATURE_DEW,
    val chimeRepeatCount: Int = 1,
    val alertsEnabled: Boolean = true,
    val hasRequestedInitialPermissions: Boolean = false
)

class SettingsPreferences(private val context: Context) {

    private object Keys {
        val LOCATION_MODE = stringPreferencesKey("location_mode")
        val MANUAL_CITY_ID = stringPreferencesKey("manual_city_id")
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val CITY_NAME = stringPreferencesKey("city_name")
        val TIMEZONE_ID = stringPreferencesKey("timezone_id")
        val METHOD_ID = intPreferencesKey("method_id")
        val HAS_SYNCED_ONLINE = booleanPreferencesKey("has_synced_online")
        val LAST_SYNC_MILLIS = longPreferencesKey("last_sync_millis")
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_ID = intPreferencesKey("theme_id")
        val TONE_ID = stringPreferencesKey("tone_id")
        val CHIME_REPEAT_COUNT = intPreferencesKey("chime_repeat_count")
        val ALERTS_ENABLED = booleanPreferencesKey("alerts_enabled")
        val INITIAL_PERMS_ASKED = booleanPreferencesKey("initial_perms_asked")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        val deviceTz = TimeZone.getDefault().id
        val fallbackCity = LocationZoneCatalog.defaultCityForTimezone(deviceTz)

        val savedTz = prefs[Keys.TIMEZONE_ID]
        val savedLat = prefs[Keys.LATITUDE]
        val savedLon = prefs[Keys.LONGITUDE]
        val savedCity = prefs[Keys.CITY_NAME]

        // Only use saved region if ALL coordinates and timezone were explicitly saved together
        val hasSavedRegion = savedTz != null && savedLat != null && savedLon != null && savedCity != null

        val settings = UserSettings(
            locationMode = LocationMode.fromCode(prefs[Keys.LOCATION_MODE]),
            manualCityId = prefs[Keys.MANUAL_CITY_ID] ?: fallbackCity.id,
            latitude = if (hasSavedRegion) savedLat!! else fallbackCity.latitude,
            longitude = if (hasSavedRegion) savedLon!! else fallbackCity.longitude,
            cityName = if (hasSavedRegion) savedCity!! else fallbackCity.nameEn,
            timezoneId = if (hasSavedRegion) savedTz!! else fallbackCity.timezoneId,
            methodId = prefs[Keys.METHOD_ID] ?: fallbackCity.defaultMethodId,
            hasSyncedOnline = prefs[Keys.HAS_SYNCED_ONLINE] ?: false,
            lastSyncMillis = prefs[Keys.LAST_SYNC_MILLIS] ?: 0L,
            language = SakinaLanguage.fromCode(prefs[Keys.LANGUAGE]),
            themeId = SakinaThemeId.fromId(prefs[Keys.THEME_ID] ?: SakinaThemeId.RAWDAH_EMERALD.id),
            toneId = SakinaToneId.fromCode(prefs[Keys.TONE_ID]),
            chimeRepeatCount = (prefs[Keys.CHIME_REPEAT_COUNT] ?: 1).coerceIn(1, 3),
            alertsEnabled = prefs[Keys.ALERTS_ENABLED] ?: true,
            hasRequestedInitialPermissions = prefs[Keys.INITIAL_PERMS_ASKED] ?: false
        )
        updateFastCache(settings)
        settings
    }

    private val fastCache = context.getSharedPreferences("sakina_fast_cache", Context.MODE_PRIVATE)

    fun getCachedSettings(): UserSettings {
        val deviceTz = TimeZone.getDefault().id
        val fallbackCity = LocationZoneCatalog.defaultCityForTimezone(deviceTz)

        val savedTz = fastCache.getString("timezone_id", null)
        val savedLat = if (fastCache.contains("latitude")) java.lang.Double.longBitsToDouble(fastCache.getLong("latitude", 0L)) else null
        val savedLon = if (fastCache.contains("longitude")) java.lang.Double.longBitsToDouble(fastCache.getLong("longitude", 0L)) else null
        val savedCity = fastCache.getString("city_name", null)
        val hasSavedRegion = savedTz != null && savedLat != null && savedLon != null && savedCity != null

        return UserSettings(
            locationMode = LocationMode.fromCode(fastCache.getString("location_mode", null)),
            manualCityId = fastCache.getString("manual_city_id", null) ?: fallbackCity.id,
            latitude = if (hasSavedRegion) savedLat!! else fallbackCity.latitude,
            longitude = if (hasSavedRegion) savedLon!! else fallbackCity.longitude,
            cityName = if (hasSavedRegion) savedCity!! else fallbackCity.nameEn,
            timezoneId = if (hasSavedRegion) savedTz!! else fallbackCity.timezoneId,
            methodId = if (fastCache.contains("method_id")) fastCache.getInt("method_id", fallbackCity.defaultMethodId) else fallbackCity.defaultMethodId,
            hasSyncedOnline = fastCache.getBoolean("has_synced_online", false),
            lastSyncMillis = fastCache.getLong("last_sync_millis", 0L),
            language = SakinaLanguage.fromCode(fastCache.getString("language", null)),
            themeId = SakinaThemeId.fromId(fastCache.getInt("theme_id", SakinaThemeId.RAWDAH_EMERALD.id)),
            toneId = SakinaToneId.fromCode(fastCache.getString("tone_id", null)),
            chimeRepeatCount = fastCache.getInt("chime_repeat_count", 1).coerceIn(1, 3),
            alertsEnabled = fastCache.getBoolean("alerts_enabled", true),
            hasRequestedInitialPermissions = fastCache.getBoolean("initial_perms_asked", false)
        )
    }

    private fun updateFastCache(settings: UserSettings) {
        fastCache.edit()
            .putString("location_mode", settings.locationMode.code)
            .putString("manual_city_id", settings.manualCityId)
            .putLong("latitude", java.lang.Double.doubleToRawLongBits(settings.latitude))
            .putLong("longitude", java.lang.Double.doubleToRawLongBits(settings.longitude))
            .putString("city_name", settings.cityName)
            .putString("timezone_id", settings.timezoneId)
            .putInt("method_id", settings.methodId)
            .putBoolean("has_synced_online", settings.hasSyncedOnline)
            .putLong("last_sync_millis", settings.lastSyncMillis)
            .putString("language", settings.language.code)
            .putInt("theme_id", settings.themeId.id)
            .putString("tone_id", settings.toneId.code)
            .putInt("chime_repeat_count", settings.chimeRepeatCount)
            .putBoolean("alerts_enabled", settings.alertsEnabled)
            .putBoolean("initial_perms_asked", settings.hasRequestedInitialPermissions)
            .apply()
    }

    suspend fun getSettingsSync(): UserSettings = settingsFlow.first()

    suspend fun setLocationMode(mode: LocationMode) {
        context.dataStore.edit { it[Keys.LOCATION_MODE] = mode.code }
    }

    suspend fun updateRegion(
        latitude: Double,
        longitude: Double,
        cityName: String,
        timezoneId: String,
        methodId: Int,
        manualCityId: String? = null,
        syncedOnline: Boolean = false
    ) {
        fastCache.edit()
            .putString("city_name", cityName)
            .putString("timezone_id", timezoneId)
            .putLong("latitude", java.lang.Double.doubleToRawLongBits(latitude))
            .putLong("longitude", java.lang.Double.doubleToRawLongBits(longitude))
            .putInt("method_id", methodId)
            .apply()
        context.dataStore.edit { prefs ->
            prefs[Keys.LATITUDE] = latitude
            prefs[Keys.LONGITUDE] = longitude
            prefs[Keys.CITY_NAME] = cityName
            prefs[Keys.TIMEZONE_ID] = timezoneId
            prefs[Keys.METHOD_ID] = methodId
            if (manualCityId != null) {
                prefs[Keys.MANUAL_CITY_ID] = manualCityId
            }
            prefs[Keys.HAS_SYNCED_ONLINE] = syncedOnline
            if (syncedOnline) {
                prefs[Keys.LAST_SYNC_MILLIS] = System.currentTimeMillis()
            }
        }
    }

    suspend fun markSyncedOnline(resolvedTimezoneId: String? = null) {
        context.dataStore.edit {
            if (!resolvedTimezoneId.isNullOrBlank()) {
                it[Keys.TIMEZONE_ID] = resolvedTimezoneId
            }
            it[Keys.HAS_SYNCED_ONLINE] = true
            it[Keys.LAST_SYNC_MILLIS] = System.currentTimeMillis()
        }
    }

    suspend fun setMethodId(methodId: Int) {
        fastCache.edit().putInt("method_id", methodId).apply()
        context.dataStore.edit { it[Keys.METHOD_ID] = methodId }
    }

    suspend fun setLanguage(language: SakinaLanguage) {
        fastCache.edit().putString("language", language.code).apply()
        context.dataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setTheme(themeId: SakinaThemeId) {
        fastCache.edit().putInt("theme_id", themeId.id).apply()
        context.dataStore.edit { it[Keys.THEME_ID] = themeId.id }
    }

    suspend fun setTone(toneId: SakinaToneId) {
        context.dataStore.edit { it[Keys.TONE_ID] = toneId.code }
    }

    suspend fun setChimeRepeatCount(count: Int) {
        context.dataStore.edit { it[Keys.CHIME_REPEAT_COUNT] = count.coerceIn(1, 3) }
    }

    suspend fun setAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.ALERTS_ENABLED] = enabled }
    }

    suspend fun markInitialPermissionsAsked() {
        context.dataStore.edit { it[Keys.INITIAL_PERMS_ASKED] = true }
    }
}
