package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_days")
data class PrayerDayEntity(
    @PrimaryKey val dateIso: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val hijriDay: String,
    val hijriMonthEn: String,
    val hijriMonthAr: String,
    val hijriYear: String,
    val timezoneId: String,
    val latitude: Double,
    val longitude: Double,
    val locationLabel: String,
    val methodId: Int,
    val isEstimate: Boolean,
    val fetchedAtMillis: Long
)
