package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AladhanCalendarResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: List<AladhanDayData>
)

@JsonClass(generateAdapter = true)
data class AladhanDayData(
    @Json(name = "timings") val timings: AladhanTimings,
    @Json(name = "date") val date: AladhanDateInfo,
    @Json(name = "meta") val meta: AladhanMeta
)

@JsonClass(generateAdapter = true)
data class AladhanTimings(
    @Json(name = "Fajr") val fajr: String,
    @Json(name = "Sunrise") val sunrise: String,
    @Json(name = "Dhuhr") val dhuhr: String,
    @Json(name = "Asr") val asr: String,
    @Json(name = "Maghrib") val maghrib: String,
    @Json(name = "Isha") val isha: String
)

@JsonClass(generateAdapter = true)
data class AladhanDateInfo(
    @Json(name = "gregorian") val gregorian: AladhanGregorian,
    @Json(name = "hijri") val hijri: AladhanHijri
)

@JsonClass(generateAdapter = true)
data class AladhanGregorian(
    @Json(name = "date") val date: String
)

@JsonClass(generateAdapter = true)
data class AladhanHijri(
    @Json(name = "day") val day: String,
    @Json(name = "year") val year: String,
    @Json(name = "month") val month: AladhanHijriMonth
)

@JsonClass(generateAdapter = true)
data class AladhanHijriMonth(
    @Json(name = "en") val en: String,
    @Json(name = "ar") val ar: String
)

@JsonClass(generateAdapter = true)
data class AladhanMeta(
    @Json(name = "timezone") val timezone: String
)
