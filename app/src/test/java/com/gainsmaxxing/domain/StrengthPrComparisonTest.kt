package com.gainsmaxxing.domain

import com.gainsmaxxing.domain.model.StrengthPrEntry
import com.gainsmaxxing.domain.model.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class StrengthPrComparisonTest {
    private fun entry(id: Long, kg: Float, epochMs: Long) = StrengthPrEntry(
        id = id,
        exerciseName = "Bench Press",
        oneRmKg = kg,
        loggedAt = Instant.ofEpochMilli(epochMs),
    )

    @Test
    fun latestEntry_picksMostRecent() {
        val entries = listOf(
            entry(1, 100f, 1_000L),
            entry(2, 102.5f, 2_000L),
        )
        assertEquals(102.5f, StrengthPrComparison.latestEntry(entries)!!.oneRmKg)
    }

    @Test
    fun formatDelta_firstEntry_isDash() {
        assertEquals("—", StrengthPrComparison.formatDeltaKg(100f, null, WeightUnit.KG))
    }

    @Test
    fun formatDelta_positiveChange_inKg() {
        assertEquals("+2.5 kg", StrengthPrComparison.formatDeltaKg(102.5f, 100f, WeightUnit.KG))
    }

    @Test
    fun previousEntry_excludesLatest() {
        val latest = entry(2, 102.5f, 2_000L)
        val entries = listOf(entry(1, 100f, 1_000L), latest)
        assertEquals(100f, StrengthPrComparison.previousEntry(entries, latest)!!.oneRmKg)
    }

    @Test
    fun previousEntry_noneWhenOnlyOne() {
        val latest = entry(1, 100f, 1_000L)
        assertNull(StrengthPrComparison.previousEntry(listOf(latest), latest))
    }
}
