package com.gainsmaxxing.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "strength_pr_entries",
    indices = [Index("exerciseName")],
)
data class StrengthPrEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseName: String,
    val oneRmKg: Float,
    val loggedAtEpochMs: Long,
)
