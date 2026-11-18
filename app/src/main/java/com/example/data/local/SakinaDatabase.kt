package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PrayerDayEntity::class], version = 1, exportSchema = false)
abstract class SakinaDatabase : RoomDatabase() {
    abstract fun prayerDao(): PrayerDao

    companion object {
        @Volatile
        private var INSTANCE: SakinaDatabase? = null

        fun getInstance(context: Context): SakinaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SakinaDatabase::class.java,
                    "sakina_prayers.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
