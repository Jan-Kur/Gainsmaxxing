package com.gainsmaxxing.domain

import kotlin.math.roundToInt

object SleepFormat {
    fun hoursAndMinutesToFloat(hours: Int, minutes: Int): Float =
        hours + minutes.coerceIn(0, 59) / 60f

    fun formatDuration(hours: Float): String {
        val totalMinutes = (hours * 60f).roundToInt().coerceAtLeast(0)
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        return when {
            m == 0 -> "${h}h"
            h == 0 -> "${m}m"
            else -> "${h}h ${m}m"
        }
    }
}
