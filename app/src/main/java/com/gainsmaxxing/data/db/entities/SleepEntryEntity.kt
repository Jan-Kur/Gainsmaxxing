package com.gainsmaxxing.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_entries")
data class SleepEntryEntity(
    @PrimaryKey val date: String,
    val hours: Float,
    val energyTag: String,
)
