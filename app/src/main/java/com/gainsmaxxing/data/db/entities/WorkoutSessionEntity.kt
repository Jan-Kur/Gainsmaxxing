package com.gainsmaxxing.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sessions",
    indices = [Index("status")],
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int,
    val workoutName: String,
    val status: String,
    val startedAtEpochMs: Long,
    val finishedAtEpochMs: Long?,
)
