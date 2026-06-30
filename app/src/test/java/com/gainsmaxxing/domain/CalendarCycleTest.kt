package com.gainsmaxxing.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalendarCycleTest {
    @Test
    fun emptyCatalog_alwaysReturnsNull() {
        assertNull(CalendarCycle.nextTypeId(null, emptyList()))
        assertNull(CalendarCycle.nextTypeId(1L, emptyList()))
    }

    @Test
    fun fromEmpty_returnsFirstType() {
        assertEquals(10L, CalendarCycle.nextTypeId(null, listOf(10L, 20L, 30L)))
    }

    @Test
    fun fromLast_returnsNull() {
        assertNull(CalendarCycle.nextTypeId(30L, listOf(10L, 20L, 30L)))
    }

    @Test
    fun fromMiddle_returnsNext() {
        assertEquals(20L, CalendarCycle.nextTypeId(10L, listOf(10L, 20L, 30L)))
    }

    @Test
    fun unknownCurrent_returnsFirst() {
        assertEquals(10L, CalendarCycle.nextTypeId(99L, listOf(10L, 20L, 30L)))
    }
}
