package com.gainsmaxxing.data.db.entities

import androidx.room.Entity

@Entity(
    tableName = "calendar_template_slots",
    primaryKeys = ["dayOfWeek", "slot"],
)
data class CalendarTemplateSlotEntity(
    val dayOfWeek: Int,
    val slot: String,
    val activityTypeId: Long?,
)
