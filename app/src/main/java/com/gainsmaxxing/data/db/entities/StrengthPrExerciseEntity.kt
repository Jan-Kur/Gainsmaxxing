package com.gainsmaxxing.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "strength_pr_exercises")
data class StrengthPrExerciseEntity(
    @PrimaryKey val name: String,
    val sortOrder: Int,
)
