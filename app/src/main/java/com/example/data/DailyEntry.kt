package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_entries")
data class DailyEntry(
    @PrimaryKey
    val date: String, // YYYY-MM-DD
    val textStory: String,
    val vibeColor: String? = null
)
