package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.MainViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

import androidx.compose.ui.platform.LocalContext
import com.example.utils.NotificationScheduler

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val dob by viewModel.userDob.collectAsState()
    val lifespan by viewModel.userLifespan.collectAsState()
    val themePref by viewModel.themePreference.collectAsState()
    val languagePref by viewModel.languagePreference.collectAsState()
    val morningTime by viewModel.morningReminderTime.collectAsState()
    val eveningTime by viewModel.eveningReminderTime.collectAsState()

    var showDobDialog by remember { mutableStateOf(false) }
    var showLifespanDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var showMorningDialog by remember { mutableStateOf(false) }
    var showEveningDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.settings),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                SectionHeader(stringResource(id = R.string.profile))
                SettingItem(
                    title = stringResource(id = R.string.date_of_birth),
                    subtitle = dob ?: stringResource(id = R.string.not_set),
                    icon = Icons.Default.DateRange,
                    onClick = { showDobDialog = true }
                )
                SettingItem(
                    title = stringResource(id = R.string.expected_lifespan),
                    subtitle = "$lifespan ${stringResource(id = R.string.years)}",
                    icon = Icons.Default.Favorite,
                    onClick = { showLifespanDialog = true }
                )
            }
            item {
                SectionHeader(stringResource(id = R.string.app_customization))
                
                val themeSubtitle = when (themePref) {
                    "LIGHT" -> stringResource(id = R.string.light)
                    "DARK" -> stringResource(id = R.string.dark)
                    else -> stringResource(id = R.string.system)
                }
                
                SettingItem(
                    title = stringResource(id = R.string.theme),
                    subtitle = themeSubtitle,
                    icon = Icons.Default.Palette,
                    onClick = { showThemeDialog = true }
                )
                SettingItem(
                    title = stringResource(id = R.string.language),
                    subtitle = if (languagePref == "IN") stringResource(id = R.string.indonesian) else stringResource(id = R.string.english),
                    icon = Icons.Default.Language,
                    onClick = { showLanguageDialog = true }
                )
            }
            item {
                SectionHeader(stringResource(id = R.string.reminders))
                SettingItem(
                    title = stringResource(id = R.string.morning_summary),
                    subtitle = morningTime,
                    icon = Icons.Default.WbSunny,
                    onClick = { showMorningDialog = true }
                )
                SettingItem(
                    title = stringResource(id = R.string.evening_journal),
                    subtitle = eveningTime,
                    icon = Icons.Default.NightsStay,
                    onClick = { showEveningDialog = true }
                )
            }
        }
    }

    if (showDobDialog) {
        val initialMillis = try {
            if (!dob.isNullOrEmpty()) {
                LocalDate.parse(dob).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
            } else {
                null
            }
        } catch (e: Exception) { null }
        
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDobDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        viewModel.saveUserDob(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    }
                    showDobDialog = false
                }) {
                    Text(stringResource(id = R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDobDialog = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showLifespanDialog) {
        var tempLifespan by remember { mutableStateOf(lifespan.toString()) }
        AlertDialog(
            onDismissRequest = { showLifespanDialog = false },
            title = { Text(stringResource(id = R.string.set_expected_lifespan)) },
            text = {
                OutlinedTextField(
                    value = tempLifespan,
                    onValueChange = { tempLifespan = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text(stringResource(id = R.string.years)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val newVal = tempLifespan.toIntOrNull()
                    if (newVal != null && newVal in 1..150) {
                        viewModel.saveUserLifespan(newVal)
                    }
                    showLifespanDialog = false
                }) {
                    Text(stringResource(id = R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLifespanDialog = false }) { Text(stringResource(id = R.string.cancel)) }
            }
        )
    }

    if (showThemeDialog) {
        val themes = listOf("SYSTEM", "LIGHT", "DARK")
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(id = R.string.select_theme)) },
            text = {
                Column {
                    themes.forEach { theme ->
                        val themeText = when (theme) {
                            "LIGHT" -> stringResource(id = R.string.light)
                            "DARK" -> stringResource(id = R.string.dark)
                            else -> stringResource(id = R.string.system)
                        }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.saveThemePreference(theme)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themePref == theme,
                                onClick = {
                                    viewModel.saveThemePreference(theme)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(themeText, fontSize = 18.sp)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showLanguageDialog) {
        val langs = listOf("EN" to R.string.english, "IN" to R.string.indonesian)
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(id = R.string.select_language)) },
            text = {
                Column {
                    langs.forEach { (code, nameRes) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.saveLanguagePreference(code)
                                    val localeStr = if (code == "IN") "id" else "en"
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeStr))
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = languagePref == code,
                                onClick = {
                                    viewModel.saveLanguagePreference(code)
                                    val localeStr = if (code == "IN") "id" else "en"
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeStr))
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(id = nameRes), fontSize = 18.sp)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showMorningDialog) {
        val (h, m) = try {
            val parts = morningTime.split(":")
            parts[0].toInt() to parts[1].toInt()
        } catch(e: Exception) { 8 to 0 }
        
        val timeState = rememberTimePickerState(initialHour = h, initialMinute = m)
        TimePickerDialogCustom(
            onDismissRequest = { showMorningDialog = false },
            onConfirm = {
                val formattedStr = String.format("%02d:%02d", timeState.hour, timeState.minute)
                viewModel.saveMorningReminderTime(formattedStr)
                NotificationScheduler.rescheduleMorning(context, timeState.hour, timeState.minute)
                showMorningDialog = false
            }
        ) {
            TimePicker(state = timeState)
        }
    }

    if (showEveningDialog) {
        val (h, m) = try {
            val parts = eveningTime.split(":")
            parts[0].toInt() to parts[1].toInt()
        } catch(e: Exception) { 20 to 0 }
        
        val timeState = rememberTimePickerState(initialHour = h, initialMinute = m)
        TimePickerDialogCustom(
            onDismissRequest = { showEveningDialog = false },
            onConfirm = {
                val formattedStr = String.format("%02d:%02d", timeState.hour, timeState.minute)
                viewModel.saveEveningReminderTime(formattedStr)
                NotificationScheduler.rescheduleEvening(context, timeState.hour, timeState.minute)
                showEveningDialog = false
            }
        ) {
            TimePicker(state = timeState)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        headlineContent = { Text(title, fontSize = 18.sp) },
        supportingContent = { Text(subtitle, fontSize = 14.sp) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
    )
}

// Custom wrapper since M3 doesn't have a default TimePickerDialog component built in directly for TimePicker
@Composable
fun TimePickerDialogCustom(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.fillMaxWidth(),
        title = { Text(stringResource(id = R.string.select_time)) },
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                content()
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}
