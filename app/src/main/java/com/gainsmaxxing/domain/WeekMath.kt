package com.gainsmaxxing.domain

import java.time.LocalDate

object WeekMath {
    fun todayDayIndex(): Int = LocalDate.now().dayOfWeek.value - 1

    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
}
