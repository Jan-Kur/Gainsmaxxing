package com.gainsmaxxing.ui.calendar

import androidx.compose.ui.graphics.Color
import com.gainsmaxxing.data.repository.CalendarRepository

val ActivityColorPalette: List<Color> = listOf(
    Color(0xFFFFA726),
    Color(0xFF00E676),
    Color(0xFF3D9BFF),
    Color(0xFFFF8C42),
    Color(0xFFFF4D4D),
    Color(0xFFB388FF),
    Color(0xFF4DD0E1),
    Color(0xFFFFC24D),
    Color(0xFF6BF5AD),
    Color(0xFFFF6E9C),
    Color(0xFF90A4AE),
    Color(0xFFCE93D8),
)

fun activityColor(index: Int): Color =
    ActivityColorPalette[index % CalendarRepository.ACTIVITY_COLOR_COUNT]

fun activityTypeColor(paletteIndex: Int, customColorArgb: Int? = null): Color =
    if (customColorArgb != null) Color(customColorArgb) else activityColor(paletteIndex)
