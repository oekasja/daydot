package com.example.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.UserPreferencesRepository
import com.example.data.dataStore
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class NotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val type = inputData.getString("type") ?: return Result.failure()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return Result.failure()
            }
        }

        createNotificationChannel()

        val title: String
        val message: String

        if (type == "morning") {
            val userPrefsRepo = UserPreferencesRepository(applicationContext.dataStore)
            val dobString = userPrefsRepo.userDob.first()
            val lifespanInt = userPrefsRepo.userLifespan.first()

            if (dobString.isNullOrEmpty()) {
                return Result.failure()
            }

            try {
                val birthDate = LocalDate.parse(dobString)
                val deathDate = birthDate.plusYears(lifespanInt.toLong())
                
                val daysLived = ChronoUnit.DAYS.between(birthDate, LocalDate.now()).coerceAtLeast(0)
                val daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), deathDate).coerceAtLeast(0)
                
                title = "Daily Life Update"
                message = "You have lived $daysLived days. You have $daysRemaining days left. Make it count."
            } catch (e: Exception) {
                return Result.failure()
            }
        } else {
            title = "Daily Vibe"
            message = "Time to document your day and capture your vibe."
        }

        val notification = NotificationCompat.Builder(applicationContext, "daily_life_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(type.hashCode(), notification)

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Life Notifications"
            val descriptionText = "Morning and evening daily reminders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("daily_life_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
