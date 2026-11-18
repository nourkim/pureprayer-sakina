package com.example.data.repository

import com.example.data.local.PrayerDayEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

/**
 * Compact astronomical calculator used solely as an instant offline fallback
 * until the monthly schedule is fetched from the network and cached in Room.
 */
object OfflinePrayerCalculator {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun computeDay(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        timezoneId: String,
        locationLabel: String,
        methodId: Int
    ): PrayerDayEntity {
        val tz = TimeZone.getTimeZone(timezoneId)
        val cal = (calendar.clone() as Calendar).apply { timeZone = tz }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val jd = julianDate(year, month, day) - longitude / (15.0 * 24.0)
        val (decl, eqt) = sunPosition(jd)
        val tzOffsetHours = tz.getOffset(cal.timeInMillis) / 3600000.0

        val (fajrAngle, ishaAngle, ishaMinutesAfterMaghrib) = methodParams(methodId)

        val dhuhr = 12.0 + tzOffsetHours - longitude / 15.0 - eqt
        val sunrise = dhuhr - hourAngle(latitude, decl, -0.833) / 15.0
        val sunset = dhuhr + hourAngle(latitude, decl, -0.833) / 15.0
        val fajr = dhuhr - hourAngle(latitude, decl, -fajrAngle) / 15.0
        val asr = dhuhr + asrHourAngle(latitude, decl, 1.0) / 15.0
        val maghrib = sunset
        val isha = if (ishaMinutesAfterMaghrib != null) {
            maghrib + ishaMinutesAfterMaghrib / 60.0
        } else {
            dhuhr + hourAngle(latitude, decl, -ishaAngle) / 15.0
        }

        val fmt = (isoFormat.clone() as SimpleDateFormat).apply { timeZone = tz }
        val dateIso = fmt.format(cal.time)
        val hijri = approximateHijri(year, month, day)

        return PrayerDayEntity(
            dateIso = dateIso,
            fajr = formatHours(fajr),
            sunrise = formatHours(sunrise),
            dhuhr = formatHours(dhuhr),
            asr = formatHours(asr),
            maghrib = formatHours(maghrib),
            isha = formatHours(isha),
            hijriDay = hijri.first.toString(),
            hijriMonthEn = hijri.second,
            hijriMonthAr = hijri.third,
            hijriYear = hijri.fourth.toString(),
            timezoneId = timezoneId,
            latitude = latitude,
            longitude = longitude,
            locationLabel = locationLabel,
            methodId = methodId,
            isEstimate = true,
            fetchedAtMillis = System.currentTimeMillis()
        )
    }

    private fun methodParams(methodId: Int): Triple<Double, Double, Double?> = when (methodId) {
        4 -> Triple(18.5, 0.0, 90.0) // Umm Al-Qura
        5 -> Triple(19.5, 17.5, null) // Egyptian
        2 -> Triple(15.0, 15.0, null) // ISNA
        1 -> Triple(18.0, 18.0, null) // Karachi
        12 -> Triple(12.0, 12.0, null) // UOIF
        8 -> Triple(19.5, 0.0, 90.0) // Gulf
        else -> Triple(18.0, 17.0, null) // MWL / Diyanet
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = Math.toRadians(fixAngle(357.529 + 0.98560028 * d))
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = Math.toRadians(fixAngle(q + 1.915 * sin(g) + 0.020 * sin(2 * g)))
        val e = Math.toRadians(23.439 - 0.00000036 * d)
        val ra = Math.toDegrees(atan2(cos(e) * sin(l), cos(l))) / 15.0
        val eqt = q / 15.0 - fixHour(ra)
        val decl = Math.toDegrees(asin(sin(e) * sin(l)))
        return decl to eqt
    }

    private fun hourAngle(lat: Double, decl: Double, altitudeDeg: Double): Double {
        val latR = Math.toRadians(lat)
        val declR = Math.toRadians(decl)
        val altR = Math.toRadians(altitudeDeg)
        val cosVal = ((sin(altR) - sin(latR) * sin(declR)) / (cos(latR) * cos(declR)))
            .coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(cosVal))
    }

    private fun asrHourAngle(lat: Double, decl: Double, shadowFactor: Double): Double {
        val latR = Math.toRadians(lat)
        val declR = Math.toRadians(decl)
        val angle = Math.toDegrees(atan(1.0 / (shadowFactor + tan(kotlin.math.abs(latR - declR)))))
        return hourAngle(lat, decl, angle)
    }

    private fun fixAngle(a: Double): Double {
        var res = a % 360.0
        if (res < 0) res += 360.0
        return res
    }

    private fun fixHour(h: Double): Double {
        var res = h % 24.0
        if (res < 0) res += 24.0
        return res
    }

    private fun formatHours(hours: Double): String {
        val fixed = fixHour(hours + 0.5 / 60.0)
        val h = floor(fixed).toInt().coerceIn(0, 23)
        val m = floor((fixed - h) * 60.0).toInt().coerceIn(0, 59)
        return String.format(Locale.US, "%02d:%02d", h, m)
    }

    private data class HijriQuad(val first: Int, val second: String, val third: String, val fourth: Int)

    private fun approximateHijri(year: Int, month: Int, day: Int): HijriQuad {
        val jd = julianDate(year, month, day).toLong() + 1
        var l = jd - 1948440L + 10632L
        val n = (l - 1) / 10631L
        l = l - 10631L * n + 354L
        val j = ((10985L - l) / 5316L) * ((50L * l) / 17719L) +
            (l / 5670L) * ((43L * l) / 15238L)
        l = l - ((30L - j) / 15L) * ((17719L * j) / 50L) -
            (j / 16L) * ((15238L * j) / 43L) + 29L
        val hMonth = ((24L * l) / 709L).toInt().coerceIn(1, 12)
        val hDay = (l - (709L * hMonth) / 24L).toInt().coerceIn(1, 30)
        val hYear = (30L * n + j - 30L).toInt()
        val monthsEn = listOf(
            "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
            "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
            "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
        )
        val monthsAr = listOf(
            "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
            "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
            "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
        )
        return HijriQuad(hDay, monthsEn[hMonth - 1], monthsAr[hMonth - 1], hYear)
    }
}
