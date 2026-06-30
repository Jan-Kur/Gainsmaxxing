package com.gainsmaxxing.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "strength_pr_selection")
data class StrengthPrSelectionEntity(
    @PrimaryKey val exerciseName: String,
    val sortOrder: Int,
)
