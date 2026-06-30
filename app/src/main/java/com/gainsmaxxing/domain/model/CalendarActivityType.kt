package com.gainsmaxxing.domain.model

data class CalendarActivityType(
    val id: Long,
    val name: String,
    val colorPaletteIndex: Int,
    val customColorArgb: Int? = null,
    val iconKey: String,
    val sortOrder: Int,
)
