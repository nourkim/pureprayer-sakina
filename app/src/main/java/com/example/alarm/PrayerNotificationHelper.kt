package com.example.alarm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.PrayerType
import com.example.data.model.SakinaToneId
import java.util.Locale

object PrayerNotificationHelper {

    private const val NOTIFICATION_ID_BASE = 7100

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val audioAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        SakinaToneId.entries.forEach { tone ->
            val soundUri = Uri.parse("android.resource://${context.packageName}/${tone.rawResId}")
            val channel = NotificationChannel(
                tone.channelId,
                context.getString(tone.channelNameRes),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notif_channel_desc)
                setSound(soundUri, audioAttrs)
                enableVibration(true)
                vibrationPattern = longArrayOf(0L, 140L)
                setShowBadge(false)
            }
            nm.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun postSingleChimeNotification(
        context: Context,
        prayerType: PrayerType,
        prayerTimeText: String,
        locationLabel: String,
        tone: SakinaToneId
    ) {
        ensureChannels(context)
        if (!hasNotificationPermission(context)) return

        // Use system locale to format the notification in the language of the Android OS
        val systemLocale = Locale.getDefault()
        val sysConfig = Configuration(context.resources.configuration).apply {
            setLocale(systemLocale)
            setLayoutDirection(systemLocale)
        }
        val sysContext = context.createConfigurationContext(sysConfig)

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            prayerType.id,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prayerName = sysContext.getString(prayerType.labelRes)
        val title = sysContext.getString(R.string.notif_prayer_title, prayerName)
        val body = sysContext.getString(R.string.notif_prayer_body, prayerTimeText, locationLabel)
        val appName = sysContext.getString(R.string.app_name)
        val soundUri = Uri.parse("android.resource://${context.packageName}/${tone.rawResId}")

        val bigStyle = NotificationCompat.BigTextStyle()
            .setBigContentTitle("$title • $prayerTimeText")
            .bigText("$body\n$appName • ${sysContext.getString(R.string.notif_channel_desc)}")
            .setSummaryText(locationLabel)

        val notification = NotificationCompat.Builder(context, tone.channelId)
            .setSmallIcon(R.drawable.ic_stat_sakina)
            .setContentTitle("$title • $prayerTimeText")
            .setContentText(body)
            .setStyle(bigStyle)
            .setColor(0xFF0F766E.toInt())
            .setColorized(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis())
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        runCatching {
            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID_BASE + prayerType.id, notification)
        }
    }
}
