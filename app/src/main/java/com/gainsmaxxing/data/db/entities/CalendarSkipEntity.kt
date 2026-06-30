package com.gainsmaxxing.data.db.entities

import androidx.room.Entity

@Entity(
    tableName = "calendar_skip_overrides",
    primaryKeys = ["date", "slot"],
)
data class CalendarSkipEntity(
    val date: String,
    val slot: String,
)
