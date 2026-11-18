package com.example.ui

import android.app.Application
import android.content.Context
import android.os.PowerManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SakinaApp
import com.example.alarm.PrayerAlarmScheduler
import com.example.alarm.PrayerNotificationHelper
import com.example.alarm.TonePreviewPlayer
import com.example.data.local.PrayerDayEntity
import com.example.data.local.UserSettings
import com.example.data.location.LocationZoneCatalog
import com.example.data.model.CityZone
import com.example.data.model.LocationMode
import com.example.data.model.PrayerType
import com.example.data.model.SakinaLanguage
import com.example.data.model.SakinaThemeId
import com.example.data.model.SakinaToneId
import com.example.data.repository.PrayerRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class PrayerRowUi(
    val type: PrayerType,
    val timeText: String,
    val isNext: Boolean,
    val isPassed: Boolean
)

data class BackgroundStatusUi(
    val isBatteryUnrestricted: Boolean = false,
    val canExactAlarm: Boolean = true,
    val hasNotifications: Boolean = true
)

data class MainUiState(
    val settings: UserSettings = UserSettings(),
    val todayPrayers: PrayerDayEntity? = null,
    val rows: List<PrayerRowUi> = emptyList(),
    val nextPrayerType: PrayerType = PrayerType.FAJR,
    val nextPrayerTimeText: String = "--:--",
    val nextPrayerIsTomorrow: Boolean = false,
    val countdownText: String = "00:00:00",
    val progressToNext: Float = 0f,
    val gregorianDateText: String = "",
    val hijriDateText: String = "",
    val isSyncing: Boolean = false,
    val syncError: String? = null,
    val backgroundStatus: BackgroundStatusUi = BackgroundStatusUi()
)

class PrayerViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SakinaApp
    private val container = app.container
    private val prefs = container.settingsPreferences
    private val repository = container.prayerRepository
    private val alarmScheduler = container.alarmScheduler
    private val tonePreviewPlayer = TonePreviewPlayer()

    private val isSyncing = MutableStateFlow(false)
    private val syncError = MutableStateFlow<String?>(null)
    private val bgStatus = MutableStateFlow(readBackgroundStatus())

    private val tickerFlow = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1000L)
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val todayScheduleFlow = prefs.settingsFlow
        .flatMapLatest { settings ->
            repository.observeToday(settings.timezoneId)
        }

    val uiState: StateFlow<MainUiState> = combine(
        prefs.settingsFlow,
        todayScheduleFlow,
        tickerFlow,
        isSyncing,
        syncError,
        bgStatus
    ) { values ->
        val settings = values[0] as UserSettings
        val today = values[1] as PrayerDayEntity?
        val nowMillis = values[2] as Long
        val syncing = values[3] as Boolean
        val error = values[4] as String?
        val status = values[5] as BackgroundStatusUi

        buildMainUiState(
            settings = settings,
            today = today,
            nowMillis = nowMillis,
            syncing = syncing,
            error = error,
            bgStatus = status
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = buildInitialUiState()
    )

    private fun buildInitialUiState(): MainUiState {
        val cachedSettings = prefs.getCachedSettings()
        val now = System.currentTimeMillis()
        val tz = TimeZone.getTimeZone(cachedSettings.timezoneId)
        val cal = Calendar.getInstance(tz).apply { timeInMillis = now }
        val initialDay = runCatching {
            com.example.data.repository.OfflinePrayerCalculator.computeDay(
                calendar = cal,
                latitude = cachedSettings.latitude,
                longitude = cachedSettings.longitude,
                timezoneId = cachedSettings.timezoneId,
                locationLabel = cachedSettings.cityName,
                methodId = cachedSettings.methodId
            )
        }.getOrNull()

        return buildMainUiState(
            settings = cachedSettings,
            today = initialDay,
            nowMillis = now,
            syncing = false,
            error = null,
            bgStatus = readBackgroundStatus()
        )
    }

    init {
        refreshSchedule(forceGpsRefresh = false)
    }

    fun refreshSchedule(forceGpsRefresh: Boolean = false) {
        viewModelScope.launch {
            isSyncing.value = true
            syncError.value = null
            val result = repository.ensureScheduleUpToDate(forceGpsRefresh = forceGpsRefresh)
            if (result.isFailure) {
                syncError.value = result.exceptionOrNull()?.localizedMessage
            }
            alarmScheduler.rescheduleNextAlarm()
            isSyncing.value = false
        }
    }

    fun refreshBackgroundStatus() {
        bgStatus.value = readBackgroundStatus()
    }

    fun setLocationModeGps() {
        viewModelScope.launch {
            prefs.setLocationMode(LocationMode.GPS)
            refreshSchedule(forceGpsRefresh = true)
        }
    }

    fun selectManualCity(city: CityZone) {
        viewModelScope.launch {
            isSyncing.value = true
            syncError.value = null
            val result = repository.selectManualCity(city)
            if (result.isFailure) {
                syncError.value = result.exceptionOrNull()?.localizedMessage
            }
            alarmScheduler.rescheduleNextAlarm()
            isSyncing.value = false
        }
    }

    fun setCalculationMethod(methodId: Int) {
        viewModelScope.launch {
            isSyncing.value = true
            repository.updateCalculationMethod(methodId)
            alarmScheduler.rescheduleNextAlarm()
            isSyncing.value = false
        }
    }

    fun setLanguage(language: SakinaLanguage) {
        viewModelScope.launch {
            prefs.setLanguage(language)
        }
    }

    fun setTheme(themeId: SakinaThemeId) {
        viewModelScope.launch {
            prefs.setTheme(themeId)
        }
    }

    fun selectAndPreviewTone(toneId: SakinaToneId) {
        viewModelScope.launch {
            val count = prefs.getSettingsSync().chimeRepeatCount
            tonePreviewPlayer.playRepeated(app, toneId, count)
            prefs.setTone(toneId)
        }
    }

    fun setChimeRepeatCount(count: Int) {
        viewModelScope.launch {
            val valid = count.coerceIn(1, 3)
            prefs.setChimeRepeatCount(valid)
            val currentTone = prefs.getSettingsSync().toneId
            tonePreviewPlayer.playRepeated(app, currentTone, valid)
        }
    }

    fun setAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setAlertsEnabled(enabled)
            alarmScheduler.rescheduleNextAlarm()
        }
    }

    fun markInitialPermissionsAsked() {
        viewModelScope.launch {
            prefs.markInitialPermissionsAsked()
        }
    }

    private fun readBackgroundStatus(): BackgroundStatusUi {
        val pm = app.getSystemService(Context.POWER_SERVICE) as PowerManager
        val unrestricted = pm.isIgnoringBatteryOptimizations(app.packageName)
        return BackgroundStatusUi(
            isBatteryUnrestricted = unrestricted,
            canExactAlarm = alarmScheduler.canScheduleExactAlarms(),
            hasNotifications = PrayerNotificationHelper.hasNotificationPermission(app)
        )
    }

    private fun buildMainUiState(
        settings: UserSettings,
        today: PrayerDayEntity?,
        nowMillis: Long,
        syncing: Boolean,
        error: String?,
        bgStatus: BackgroundStatusUi
    ): MainUiState {
        val tz = TimeZone.getTimeZone(settings.timezoneId)
        val cal = Calendar.getInstance(tz).apply { timeInMillis = nowMillis }
        val isArabic = com.example.util.LocaleHelper.isArabic(settings.language)
        val displayLocale = com.example.util.LocaleHelper.resolveLocale(settings.language)

        val gregorianFmt = SimpleDateFormat("EEE, d MMM yyyy", displayLocale).apply { timeZone = tz }
        val gregorianText = gregorianFmt.format(cal.time)

        if (today == null) {
            return MainUiState(
                settings = settings,
                gregorianDateText = gregorianText,
                isSyncing = syncing,
                syncError = error,
                backgroundStatus = bgStatus
            )
        }

        val hijriMonth = if (isArabic) today.hijriMonthAr else today.hijriMonthEn
        val hijriText = "${today.hijriDay} $hijriMonth ${today.hijriYear}"

        val todayStartCal = (cal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dayEpoch = todayStartCal.timeInMillis

        val entries = listOf(
            PrayerType.FAJR to today.fajr,
            PrayerType.SUNRISE to today.sunrise,
            PrayerType.DHUHR to today.dhuhr,
            PrayerType.ASR to today.asr,
            PrayerType.MAGHRIB to today.maghrib,
            PrayerType.ISHA to today.isha
        )

        val epochs = entries.map { (type, timeStr) ->
            Triple(
                type,
                timeStr,
                PrayerAlarmScheduler.parseTimeOnDateMillis(dayEpoch, timeStr, tz) ?: dayEpoch
            )
        }

        // Next obligatory prayer (skip Sunrise as a prayer alert, though Sunrise appears in list)
        val upcoming = epochs.firstOrNull { (type, _, epoch) ->
            type.isObligatoryAlert && epoch > nowMillis
        }

        val nextType: PrayerType
        val nextTimeStr: String
        val nextEpoch: Long
        val prevEpoch: Long
        val isTomorrow: Boolean

        if (upcoming != null) {
            nextType = upcoming.first
            nextTimeStr = upcoming.second
            nextEpoch = upcoming.third
            isTomorrow = false
            val idx = epochs.indexOfFirst { it.first == nextType }
            prevEpoch = if (idx > 0) {
                epochs[idx - 1].third
            } else {
                // Before Fajr: previous boundary is yesterday's Isha (~6 hours before Fajr or Isha - 24h)
                epochs.last().third - 24L * 3600L * 1000L
            }
        } else {
            // After Isha -> next is tomorrow's Fajr
            nextType = PrayerType.FAJR
            nextTimeStr = today.fajr
            nextEpoch = epochs.first().third + 24L * 3600L * 1000L
            prevEpoch = epochs.last().third
            isTomorrow = true
        }

        val remainingMs = (nextEpoch - nowMillis).coerceAtLeast(0L)
        val totalSpanMs = (nextEpoch - prevEpoch).coerceAtLeast(1L)
        val progress = ((nowMillis - prevEpoch).toFloat() / totalSpanMs.toFloat()).coerceIn(0f, 1f)

        val totalSec = remainingMs / 1000L
        val hrs = totalSec / 3600L
        val mins = (totalSec % 3600L) / 60L
        val secs = totalSec % 60L
        val countdown = String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)

        val rows = epochs.map { (type, timeStr, epoch) ->
            PrayerRowUi(
                type = type,
                timeText = timeStr,
                isNext = !isTomorrow && type == nextType,
                isPassed = epoch <= nowMillis
            )
        }

        return MainUiState(
            settings = settings,
            todayPrayers = today,
            rows = rows,
            nextPrayerType = nextType,
            nextPrayerTimeText = nextTimeStr,
            nextPrayerIsTomorrow = isTomorrow,
            countdownText = countdown,
            progressToNext = progress,
            gregorianDateText = gregorianText,
            hijriDateText = hijriText,
            isSyncing = syncing,
            syncError = error,
            backgroundStatus = bgStatus
        )
    }

    override fun onCleared() {
        super.onCleared()
        tonePreviewPlayer.stop()
    }
}
