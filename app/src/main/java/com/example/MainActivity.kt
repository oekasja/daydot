package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.ui.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.workers.NotificationWorker
import java.util.concurrent.TimeUnit
import java.util.Calendar

import com.example.data.dataStore
import com.example.data.UserPreferencesRepository
import com.example.utils.NotificationScheduler
import kotlinx.coroutines.flow.first
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class MainActivity : AppCompatActivity() {

  private val requestPermissionLauncher = registerForActivityResult(
      ActivityResultContracts.RequestPermission()
  ) {}

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    askNotificationPermission()
    
    val userPrefs = UserPreferencesRepository(applicationContext.dataStore)
    lifecycleScope.launch {
        val langStr = userPrefs.languagePreference.first()
        val localeStr = if (langStr == "IN") "id" else "en"
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeStr))
        
        val morningStr = userPrefs.morningReminderTime.first()
        val eveningStr = userPrefs.eveningReminderTime.first()
        try {
            val (mh, mm) = morningStr.split(":").let { it[0].toInt() to it[1].toInt() }
            val (eh, em) = eveningStr.split(":").let { it[0].toInt() to it[1].toInt() }
            NotificationScheduler.rescheduleMorning(applicationContext, mh, mm)
            NotificationScheduler.rescheduleEvening(applicationContext, eh, em)
        } catch (e: Exception) {
            NotificationScheduler.rescheduleMorning(applicationContext, 8, 0)
            NotificationScheduler.rescheduleEvening(applicationContext, 20, 0)
        }
    }

    setContent {
      val themePref by userPrefs.themePreference.collectAsState(initial = "SYSTEM")
      val darkTheme = when (themePref) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
      }

      val languagePref by userPrefs.languagePreference.collectAsState(initial = "EN")

      MyApplicationTheme(darkTheme = darkTheme) {
        MainScreen()
      }
    }
  }

  private fun askNotificationPermission() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
              PackageManager.PERMISSION_GRANTED
          ) {
              requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
          }
      }
  }
}

