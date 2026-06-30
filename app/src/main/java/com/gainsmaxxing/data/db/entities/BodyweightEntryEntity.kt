package com.gainsmaxxing.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bodyweight_entries")
data class BodyweightEntryEntity(
    @PrimaryKey val date: String,
    val weightKg: Float,
)
