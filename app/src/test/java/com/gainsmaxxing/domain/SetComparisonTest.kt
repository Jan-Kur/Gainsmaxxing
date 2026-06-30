package com.gainsmaxxing.domain

import com.gainsmaxxing.domain.model.LoggedSet
import com.gainsmaxxing.domain.model.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetComparisonTest {
    @Test
    fun prFlags_markOnlyWorkingSetsAboveHistoricalBest() {
        val sets = listOf(
            LoggedSet(exerciseId = 1, weightKg = 80f, reps = 8, isWarmup = true),
            LoggedSet(exerciseId = 1, weightKg = 100f, reps = 5, isWarmup = false),
            LoggedSet(exerciseId = 1, weightKg = 102.5f, reps = 5, isWarmup = false),
        )

        val flags = SetComparison.prFlagsForOrderedSets(sets, historicalBestKg = 100f)

        assertFalse(flags[0])
        assertFalse(flags[1])
        assertTrue(flags[2])
    }
}

class WeightFormatTest {
    @Test
    fun storesKg_convertsForLbsDisplay() {
        assertEquals(220.462f, WeightFormat.kgToDisplay(100f, WeightUnit.LBS), 0.01f)
        assertEquals(100f, WeightFormat.displayToKg(220.462f, WeightUnit.LBS), 0.01f)
    }
}
