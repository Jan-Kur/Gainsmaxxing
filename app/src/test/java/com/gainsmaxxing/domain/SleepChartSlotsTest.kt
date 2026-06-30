package com.gainsmaxxing.domain

import com.gainsmaxxing.domain.model.EnergyTag
import com.gainsmaxxing.domain.model.SleepEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SleepChartSlotsTest {
    private val today = LocalDate.of(2026, 6, 30)

    private fun entry(date: LocalDate, hours: Float) =
        SleepEntry(date, hours, EnergyTag.NEUTRAL)

    @Test
    fun singleEntry_anchorsLeft() {
        val slots = SleepChartSlots.build(listOf(entry(today, 7f)), today)
        assertEquals(30, slots.size)
        assertEquals(7f, slots[0].hours)
        assertEquals(today, slots[0].date)
        assertTrue(slots.drop(1).all { it.hours == 0f })
    }

    @Test
    fun twoConsecutiveDays_fillFromLeft() {
        val slots = SleepChartSlots.build(
            listOf(
                entry(today.minusDays(1), 6.5f),
                entry(today, 8f),
            ),
            today,
        )
        assertEquals(6.5f, slots[0].hours)
        assertEquals(8f, slots[1].hours)
        assertTrue(slots.drop(2).all { it.hours == 0f })
    }

    @Test
    fun rollingWindow_whenSpanIs30Days() {
        val first = today.minusDays(29)
        val entries = (0 until 30).map { i ->
            entry(first.plusDays(i.toLong()), 7f)
        }
        val slots = SleepChartSlots.build(entries, today)
        assertEquals(first, slots.first().date)
        assertEquals(today, slots.last().date)
        assertTrue(slots.all { it.hours == 7f })
    }
}
