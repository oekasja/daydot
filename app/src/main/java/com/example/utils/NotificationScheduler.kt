package com.example.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.workers.NotificationWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    fun rescheduleMorning(context: Context, hour: Int, minute: Int) {
        val delay = calculateDelayUntil(hour, minute)
        val request = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("type" to "morning"))
            .build()
            
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "MorningReminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun rescheduleEvening(context: Context, hour: Int, minute: Int) {
        val delay = calculateDelayUntil(hour, minute)
        val request = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("type" to "evening"))
            .build()
            
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "EveningReminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun calculateDelayUntil(targetHour: Int, targetMinute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (now.after(target)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}
