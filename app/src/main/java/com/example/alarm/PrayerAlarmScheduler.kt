package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.local.PrayerDayEntity
import com.example.data.local.SettingsPreferences
import com.example.data.model.PrayerType
import com.example.data.repository.PrayerRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class ScheduledPrayerTarget(
    val prayerType: PrayerType,
    val triggerAtMillis: Long,
    val timeText: String,
    val dateIso: String,
    val isTomorrow: Boolean
)

class PrayerAlarmScheduler(
    private val context: Context,
    private val repository: PrayerRepository,
    private val prefs: SettingsPreferences
) {

    fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return am.canScheduleExactAlarms()
    }

    suspend fun rescheduleNextAlarm() {
        val settings = prefs.getSettingsSync()
        cancelExistingAlarm()
        if (!settings.alertsEnabled) return

        val tzId = settings.timezoneId
        val todayIso = PrayerRepository.todayIsoForTimezone(tzId)
        val upcomingDays = repository.getUpcomingDaysSync(todayIso, limit = 3)
        if (upcomingDays.isEmpty()) return

        val nextTarget = findNextUpcomingPrayer(
            days = upcomingDays,
            timezoneId = tzId,
            nowMillis = System.currentTimeMillis()
        ) ?: return

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_ID, nextTarget.prayerType.id)
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_TIME, nextTarget.timeText)
            putExtra(PrayerAlarmReceiver.EXTRA_LOCATION_LABEL, settings.cityName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTarget.triggerAtMillis,
                    pendingIntent
                )
            } else {
                am.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTarget.triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            am.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextTarget.triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelExistingAlarm() {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            am.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    companion object {
        private const val ALARM_REQUEST_CODE = 4091

        fun findNextUpcomingPrayer(
            days: List<PrayerDayEntity>,
            timezoneId: String,
            nowMillis: Long
        ): ScheduledPrayerTarget? {
            val tz = TimeZone.getTimeZone(timezoneId)
            val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = tz }
            val todayIso = dateFmt.format(Calendar.getInstance(tz).time)

            for (day in days) {
                val parsedDate = runCatching { dateFmt.parse(day.dateIso) }.getOrNull() ?: continue
                val prayers = listOf(
                    PrayerType.FAJR to day.fajr,
                    PrayerType.DHUHR to day.dhuhr,
                    PrayerType.ASR to day.asr,
                    PrayerType.MAGHRIB to day.maghrib,
                    PrayerType.ISHA to day.isha
                )
                for ((type, timeStr) in prayers) {
                    val triggerMillis = parseTimeOnDateMillis(parsedDate.time, timeStr, tz) ?: continue
                    if (triggerMillis > nowMillis + 1500L) {
                        return ScheduledPrayerTarget(
                            prayerType = type,
                            triggerAtMillis = triggerMillis,
                            timeText = timeStr,
                            dateIso = day.dateIso,
                            isTomorrow = day.dateIso != todayIso
                        )
                    }
                }
            }
            return null
        }

        fun parseTimeOnDateMillis(dayEpochMillis: Long, hhMm: String, tz: TimeZone): Long? {
            val parts = hhMm.split(":")
            if (parts.size != 2) return null
            val h = parts[0].toIntOrNull() ?: return null
            val m = parts[1].toIntOrNull() ?: return null
            val cal = Calendar.getInstance(tz).apply {
                timeInMillis = dayEpochMillis
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }
    }
}
