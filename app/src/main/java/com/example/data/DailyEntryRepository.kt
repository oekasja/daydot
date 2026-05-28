package com.example.data

import kotlinx.coroutines.flow.Flow

class DailyEntryRepository(private val dailyEntryDao: DailyEntryDao) {
    val allEntries: Flow<List<DailyEntry>> = dailyEntryDao.getAllEntries()

    fun getEntryByDate(date: String): Flow<DailyEntry?> {
        return dailyEntryDao.getEntryByDate(date)
    }

    suspend fun insertEntry(entry: DailyEntry) {
        dailyEntryDao.insertEntry(entry)
    }

    suspend fun deleteEntry(date: String) {
        dailyEntryDao.deleteEntry(date)
    }
}
