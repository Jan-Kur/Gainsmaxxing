package com.gainsmaxxing.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "split_days")
data class SplitDayEntity(
    @PrimaryKey val dayOfWeek: Int,
    val workoutName: String?,
)
