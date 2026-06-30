package com.gainsmaxxing.domain

import com.gainsmaxxing.domain.model.EnergyTag
import com.gainsmaxxing.domain.model.SleepEntry
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object SleepChartSlots {
    const val SLOT_COUNT = 30

    fun build(entries: List<SleepEntry>, today: LocalDate = LocalDate.now()): List<SleepEntry> {
        val loggedByDate = entries
            .filter { it.hours > 0f }
            .associateBy { it.date }

        if (loggedByDate.isEmpty()) {
            return List(SLOT_COUNT) { emptySlot(today) }
        }

        val firstLogDate = loggedByDate.keys.min()
        val daysSinceFirst = ChronoUnit.DAYS.between(firstLogDate, today).toInt() + 1

        return if (daysSinceFirst >= SLOT_COUNT) {
            // Rolling latest 30 calendar days — oldest on the left, today on the right.
            (SLOT_COUNT - 1 downTo 0).map { offset ->
                val date = today.minusDays(offset.toLong())
                loggedByDate[date] ?: emptySlot(date)
            }
        } else {
            // Anchor at the first log — grow rightward; future days stay empty on the right.
            (0 until SLOT_COUNT).map { index ->
                val date = firstLogDate.plusDays(index.toLong())
                when {
                    date.isAfter(today) -> emptySlot(date)
                    else -> loggedByDate[date] ?: emptySlot(date)
                }
            }
        }
    }

    private fun emptySlot(date: LocalDate) =
        SleepEntry(date = date, hours = 0f, energyTag = EnergyTag.NEUTRAL)
}
