package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.example.SakinaApp
import com.example.data.model.PrayerType
import com.example.data.model.SakinaToneId
import com.example.util.LocaleHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val app = context.applicationContext as SakinaApp
        val prayerId = intent.getIntExtra(EXTRA_PRAYER_ID, -1)
        val prayerTime = intent.getStringExtra(EXTRA_PRAYER_TIME) ?: ""
        val locationLabel = intent.getStringExtra(EXTRA_LOCATION_LABEL) ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = app.container.settingsPreferences.getSettingsSync()
                val prayerType = PrayerType.entries.firstOrNull { it.id == prayerId }

                if (settings.alertsEnabled && prayerType != null && prayerType.isObligatoryAlert) {
                    val localizedContext = LocaleHelper.wrapContext(context, settings.language)
                    PrayerNotificationHelper.postSingleChimeNotification(
                        context = localizedContext,
                        prayerType = prayerType,
                        prayerTimeText = prayerTime,
                        locationLabel = locationLabel.ifBlank { settings.cityName },
                        tone = settings.toneId
                    )

                    // Repeat chime if configured (1 to 3 times)
                    if (settings.chimeRepeatCount > 1) {
                        for (i in 2..settings.chimeRepeatCount) {
                            delay(3300L)
                            playSubsequentChime(context, settings.toneId)
                        }
                    }
                }

                app.container.alarmScheduler.rescheduleNextAlarm()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun playSubsequentChime(context: Context, tone: SakinaToneId) {
        runCatching {
            val uri = Uri.parse("android.resource://${context.packageName}/${tone.rawResId}")
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, uri)
                isLooping = false
                setOnCompletionListener { it.release() }
                setOnErrorListener { it, _, _ -> it.release(); true }
                prepare()
                start()
            }
        }
    }

    companion object {
        const val EXTRA_PRAYER_ID = "extra_prayer_id"
        const val EXTRA_PRAYER_TIME = "extra_prayer_time"
        const val EXTRA_LOCATION_LABEL = "extra_location_label"
    }
}
