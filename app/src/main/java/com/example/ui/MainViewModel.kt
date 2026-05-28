package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DailyEntry
import com.example.data.DailyEntryRepository
import com.example.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi

class MainViewModel(
    private val entryRepository: DailyEntryRepository,
    private val userPrefsRepository: UserPreferencesRepository
) : ViewModel() {

    private val _randomEntryDate = MutableStateFlow<String?>(null)
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val randomEntry: StateFlow<DailyEntry?> = _randomEntryDate
        .flatMapLatest { date ->
            if (date != null) {
                entryRepository.getEntryByDate(date)
            } else {
                kotlinx.coroutines.flow.flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun fetchRandomEntry(excludeDate: String? = null) {
        val currentEntries = if (excludeDate != null) allEntries.value.filter { it.date != excludeDate } else allEntries.value
        if (currentEntries.isNotEmpty()) {
            _randomEntryDate.value = currentEntries.random().date
        } else {
            _randomEntryDate.value = null
        }
    }

    val allEntries: StateFlow<List<DailyEntry>> = entryRepository.allEntries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userDob: StateFlow<String?> = userPrefsRepository.userDob
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val userLifespan: StateFlow<Int> = userPrefsRepository.userLifespan
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 80
        )

    val themePreference: StateFlow<String> = userPrefsRepository.themePreference
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "SYSTEM"
        )

    val languagePreference: StateFlow<String> = userPrefsRepository.languagePreference
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "EN"
        )

    val morningReminderTime: StateFlow<String> = userPrefsRepository.morningReminderTime
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "08:00"
        )

    val eveningReminderTime: StateFlow<String> = userPrefsRepository.eveningReminderTime
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "20:00"
        )

    val daysLived: StateFlow<Long?> = userDob.map { dob ->
        if (dob.isNullOrEmpty()) null else {
            try {
                val birthDate = LocalDate.parse(dob)
                ChronoUnit.DAYS.between(birthDate, LocalDate.now()).coerceAtLeast(0)
            } catch (e: Exception) {
                null
            }
        }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)

    val daysRemaining: StateFlow<Long?> = combine(userDob, userLifespan) { dob, lifespan ->
        if (dob.isNullOrEmpty()) null else {
            try {
                val birthDate = LocalDate.parse(dob)
                val deathDate = birthDate.plusYears(lifespan.toLong())
                ChronoUnit.DAYS.between(LocalDate.now(), deathDate).coerceAtLeast(0)
            } catch (e: Exception) {
                null
            }
        }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)

    fun saveUserDob(dob: String) {
        viewModelScope.launch {
            userPrefsRepository.saveUserDob(dob)
        }
    }

    fun saveUserLifespan(lifespan: Int) {
        viewModelScope.launch {
            userPrefsRepository.saveUserLifespan(lifespan)
        }
    }

    fun saveThemePreference(theme: String) {
        viewModelScope.launch {
            userPrefsRepository.saveThemePreference(theme)
        }
    }

    fun saveLanguagePreference(language: String) {
        viewModelScope.launch {
            userPrefsRepository.saveLanguagePreference(language)
        }
    }

    fun saveMorningReminderTime(time: String) {
        viewModelScope.launch {
            userPrefsRepository.saveMorningReminderTime(time)
        }
    }

    fun saveEveningReminderTime(time: String) {
        viewModelScope.launch {
            userPrefsRepository.saveEveningReminderTime(time)
        }
    }

    fun insertEntry(entry: DailyEntry) {
        viewModelScope.launch {
            entryRepository.insertEntry(entry)
        }
    }

    fun deleteEntry(date: String) {
        viewModelScope.launch {
            entryRepository.deleteEntry(date)
            if (_randomEntryDate.value == date) {
                _randomEntryDate.value = null
                fetchRandomEntry(excludeDate = date)
            }
        }
    }

    fun getEntryByDate(date: String): kotlinx.coroutines.flow.Flow<DailyEntry?> {
        return entryRepository.getEntryByDate(date)
    }
}
