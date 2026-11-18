package com.example.data.location

import com.example.data.model.CityZone
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object LocationZoneCatalog {

    val cities: List<CityZone> = listOf(
        CityZone("makkah", "Makkah", "مكة المكرمة", "Saudi Arabia", "السعودية", 21.4225, 39.8262, "Asia/Riyadh", 4),
        CityZone("madinah", "Madinah", "المدينة المنورة", "Saudi Arabia", "السعودية", 24.4672, 39.6112, "Asia/Riyadh", 4),
        CityZone("riyadh", "Riyadh", "الرياض", "Saudi Arabia", "السعودية", 24.7136, 46.6753, "Asia/Riyadh", 4),
        CityZone("jeddah", "Jeddah", "جدة", "Saudi Arabia", "السعودية", 21.5433, 39.1728, "Asia/Riyadh", 4),
        CityZone("dammam", "Dammam", "الدمام", "Saudi Arabia", "السعودية", 26.4207, 50.0888, "Asia/Riyadh", 4),
        CityZone("cairo", "Cairo", "القاهرة", "Egypt", "مصر", 30.0444, 31.2357, "Africa/Cairo", 5),
        CityZone("alexandria", "Alexandria", "الإسكندرية", "Egypt", "مصر", 31.2001, 29.9187, "Africa/Cairo", 5),
        CityZone("casablanca", "Casablanca", "الدار البيضاء", "Morocco", "المغرب", 33.5731, -7.5898, "Africa/Casablanca", 21),
        CityZone("rabat", "Rabat", "الرباط", "Morocco", "المغرب", 34.0209, -6.8416, "Africa/Casablanca", 21),
        CityZone("marrakesh", "Marrakesh", "مراكش", "Morocco", "المغرب", 31.6295, -7.9811, "Africa/Casablanca", 21),
        CityZone("algiers", "Algiers", "الجزائر", "Algeria", "الجزائر", 36.7538, 3.0588, "Africa/Algiers", 3),
        CityZone("oran", "Oran", "وهران", "Algeria", "الجزائر", 35.6977, -0.6308, "Africa/Algiers", 3),
        CityZone("constantine", "Constantine", "قسنطينة", "Algeria", "الجزائر", 36.3650, 6.6147, "Africa/Algiers", 3),
        CityZone("tunis", "Tunis", "تونس", "Tunisia", "تونس", 36.8065, 10.1815, "Africa/Tunis", 3),
        CityZone("tripoli", "Tripoli", "طرابلس", "Libya", "ليبيا", 32.8872, 13.1913, "Africa/Tripoli", 5),
        CityZone("amman", "Amman", "عمّان", "Jordan", "الأردن", 31.9454, 35.9284, "Asia/Amman", 23),
        CityZone("jerusalem", "Al-Quds", "القدس", "Palestine", "فلسطين", 31.7683, 35.2137, "Asia/Jerusalem", 3),
        CityZone("beirut", "Beirut", "بيروت", "Lebanon", "لبنان", 33.8938, 35.5018, "Asia/Beirut", 3),
        CityZone("damascus", "Damascus", "دمشق", "Syria", "سوريا", 33.5138, 36.2765, "Asia/Damascus", 3),
        CityZone("baghdad", "Baghdad", "بغداد", "Iraq", "العراق", 33.3152, 44.3661, "Asia/Baghdad", 3),
        CityZone("kuwait", "Kuwait City", "الكويت", "Kuwait", "الكويت", 29.3759, 47.9774, "Asia/Kuwait", 9),
        CityZone("doha", "Doha", "الدوحة", "Qatar", "قطر", 25.2854, 51.5310, "Asia/Qatar", 10),
        CityZone("manama", "Manama", "المنامة", "Bahrain", "البحرين", 26.2285, 50.5860, "Asia/Bahrain", 8),
        CityZone("abu_dhabi", "Abu Dhabi", "أبوظبي", "UAE", "الإمارات", 24.4539, 54.3773, "Asia/Dubai", 16),
        CityZone("dubai", "Dubai", "دبي", "UAE", "الإمارات", 25.2048, 55.2708, "Asia/Dubai", 16),
        CityZone("muscat", "Muscat", "مسقط", "Oman", "عُمان", 23.5880, 58.3829, "Asia/Muscat", 8),
        CityZone("sanaa", "Sana'a", "صنعاء", "Yemen", "اليمن", 15.3694, 44.1910, "Asia/Aden", 4),
        CityZone("khartoum", "Khartoum", "الخرطوم", "Sudan", "السودان", 15.5007, 32.5599, "Africa/Khartoum", 5),
        CityZone("istanbul", "Istanbul", "إسطنبول", "Turkey", "تركيا", 41.0082, 28.9784, "Europe/Istanbul", 13),
        CityZone("ankara", "Ankara", "أنقرة", "Turkey", "تركيا", 39.9334, 32.8597, "Europe/Istanbul", 13),
        CityZone("kuala_lumpur", "Kuala Lumpur", "كوالالمبور", "Malaysia", "ماليزيا", 3.1390, 101.6869, "Asia/Kuala_Lumpur", 17),
        CityZone("jakarta", "Jakarta", "جاكرتا", "Indonesia", "إندونيسيا", -6.2088, 106.8456, "Asia/Jakarta", 20),
        CityZone("singapore", "Singapore", "سنغافورة", "Singapore", "سنغافورة", 1.3521, 103.8198, "Asia/Singapore", 11),
        CityZone("karachi", "Karachi", "كراتشي", "Pakistan", "باكستان", 24.8607, 67.0011, "Asia/Karachi", 1),
        CityZone("london", "London", "لندن", "UK", "المملكة المتحدة", 51.5072, -0.1276, "Europe/London", 3),
        CityZone("paris", "Paris", "باريس", "France", "فرنسا", 48.8566, 2.3522, "Europe/Paris", 12),
        CityZone("marseille", "Marseille", "مرسيليا", "France", "فرنسا", 43.2965, 5.3698, "Europe/Paris", 12),
        CityZone("berlin", "Berlin", "برلين", "Germany", "ألمانيا", 52.5200, 13.4050, "Europe/Berlin", 3),
        CityZone("madrid", "Madrid", "مدريد", "Spain", "إسبانيا", 40.4168, -3.7038, "Europe/Madrid", 3),
        CityZone("new_york", "New York", "نيويورك", "USA", "أمريكا", 40.7128, -74.0060, "America/New_York", 2),
        CityZone("chicago", "Chicago", "شيكاغو", "USA", "أمريكا", 41.8781, -87.6298, "America/Chicago", 2),
        CityZone("los_angeles", "Los Angeles", "لوس أنجلوس", "USA", "أمريكا", 34.0522, -118.2437, "America/Los_Angeles", 2),
        CityZone("toronto", "Toronto", "تورونتو", "Canada", "كندا", 43.6532, -79.3832, "America/Toronto", 2),
        CityZone("sydney", "Sydney", "سيدني", "Australia", "أستراليا", -33.8688, 151.2093, "Australia/Sydney", 3)
    )

    fun findById(id: String): CityZone? = cities.firstOrNull { it.id == id }

    fun defaultCityForTimezone(timezoneId: String): CityZone {
        val exact = cities.firstOrNull { it.timezoneId.equals(timezoneId, ignoreCase = true) }
        if (exact != null) return exact

        val targetTz = TimeZone.getTimeZone(timezoneId)
        val now = System.currentTimeMillis()
        val targetOffset = targetTz.getOffset(now)
        val continent = timezoneId.substringBefore("/", "")

        // Prefer a city with matching UTC offset and same continent
        val sameContinentAndOffset = cities.firstOrNull { city ->
            city.timezoneId.startsWith("$continent/") &&
                TimeZone.getTimeZone(city.timezoneId).getOffset(now) == targetOffset
        }
        if (sameContinentAndOffset != null) return sameContinentAndOffset

        // Otherwise find the city with the closest UTC offset
        return cities.minByOrNull { city ->
            abs(TimeZone.getTimeZone(city.timezoneId).getOffset(now) - targetOffset)
        } ?: cities.first { it.id == "makkah" }
    }

    fun recommendedMethodForTimezone(timezoneId: String): Int {
        val exact = cities.firstOrNull { it.timezoneId.equals(timezoneId, ignoreCase = true) }
        if (exact != null) return exact.defaultMethodId

        return when {
            timezoneId.startsWith("America/") -> 2
            timezoneId.startsWith("Europe/Paris") -> 12
            timezoneId.startsWith("Europe/Istanbul") -> 13
            timezoneId.startsWith("Africa/Casablanca") -> 21
            timezoneId.startsWith("Africa/Cairo") -> 5
            timezoneId.startsWith("Asia/Riyadh") -> 4
            timezoneId.startsWith("Asia/Dubai") -> 16
            timezoneId.startsWith("Asia/Qatar") -> 10
            timezoneId.startsWith("Asia/Kuwait") -> 9
            timezoneId.startsWith("Asia/Amman") -> 23
            timezoneId.startsWith("Asia/Kuala_Lumpur") -> 17
            timezoneId.startsWith("Asia/Jakarta") -> 20
            timezoneId.startsWith("Asia/Singapore") -> 11
            timezoneId.startsWith("Asia/Karachi") -> 1
            else -> 3
        }
    }

    fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun nearestCity(lat: Double, lon: Double): CityZone {
        return cities.minByOrNull { distanceKm(lat, lon, it.latitude, it.longitude) }
            ?: cities.first()
    }
}
