package com.example

import android.app.Application
import com.example.alarm.PrayerAlarmScheduler
import com.example.alarm.PrayerNotificationHelper
import com.example.data.local.SakinaDatabase
import com.example.data.local.SettingsPreferences
import com.example.data.location.DeviceLocationHelper
import com.example.data.remote.AladhanApiService
import com.example.data.repository.PrayerRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(application: Application) {
    val database: SakinaDatabase by lazy { SakinaDatabase.getInstance(application) }
    val settingsPreferences: SettingsPreferences by lazy { SettingsPreferences(application) }
    val locationHelper: DeviceLocationHelper by lazy { DeviceLocationHelper(application) }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    val aladhanApi: AladhanApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.aladhan.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AladhanApiService::class.java)
    }

    val prayerRepository: PrayerRepository by lazy {
        PrayerRepository(
            prayerDao = database.prayerDao(),
            prefs = settingsPreferences,
            apiService = aladhanApi,
            locationHelper = locationHelper
        )
    }

    val alarmScheduler: PrayerAlarmScheduler by lazy {
        PrayerAlarmScheduler(
            context = application,
            repository = prayerRepository,
            prefs = settingsPreferences
        )
    }
}

class SakinaApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        PrayerNotificationHelper.ensureChannels(this)
    }
}
