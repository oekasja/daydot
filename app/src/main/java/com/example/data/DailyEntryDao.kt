package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyEntryDao {
    @Query("SELECT * FROM daily_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<DailyEntry>>

    @Query("SELECT * FROM daily_entries WHERE date = :date")
    fun getEntryByDate(date: String): Flow<DailyEntry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DailyEntry)

    @Query("DELETE FROM daily_entries WHERE date = :date")
    suspend fun deleteEntry(date: String)
}
