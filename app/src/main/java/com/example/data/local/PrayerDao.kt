package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayer_days WHERE dateIso = :dateIso LIMIT 1")
    fun observeDay(dateIso: String): Flow<PrayerDayEntity?>

    @Query("SELECT * FROM prayer_days WHERE dateIso = :dateIso LIMIT 1")
    suspend fun getDaySync(dateIso: String): PrayerDayEntity?

    @Query("SELECT * FROM prayer_days WHERE dateIso >= :fromDateIso ORDER BY dateIso ASC LIMIT :limit")
    suspend fun getUpcomingDaysSync(fromDateIso: String, limit: Int = 7): List<PrayerDayEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDays(days: List<PrayerDayEntity>)

    @Query("DELETE FROM prayer_days WHERE dateIso < :cutoffDateIso")
    suspend fun pruneOldDays(cutoffDateIso: String)

    @Query("DELETE FROM prayer_days")
    suspend fun clearAll()
}
