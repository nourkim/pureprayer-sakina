package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.SakinaApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SystemEventReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_TIMEZONE_CHANGED &&
            action != Intent.ACTION_TIME_CHANGED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        val pendingResult = goAsync()
        val app = context.applicationContext as SakinaApp
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (action == Intent.ACTION_TIMEZONE_CHANGED) {
                    app.container.prayerRepository.ensureScheduleUpToDate(forceGpsRefresh = true)
                }
                app.container.alarmScheduler.rescheduleNextAlarm()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
