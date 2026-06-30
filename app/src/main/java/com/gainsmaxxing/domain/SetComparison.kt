package com.gainsmaxxing.domain

import com.gainsmaxxing.domain.model.LoggedSet

object SetComparison {
    fun topWorkingWeightKg(sets: List<LoggedSet>): Float? =
        sets.filterNot { it.isWarmup }.maxOfOrNull { it.weightKg }

    fun isSetPr(
        weightKg: Float,
        historicalBestKg: Float,
        currentSessionSets: List<LoggedSet>,
        isWarmup: Boolean,
    ): Boolean {
        if (isWarmup) return false
        val sessionBest = topWorkingWeightKg(currentSessionSets) ?: 0f
        val priorBest = maxOf(historicalBestKg, sessionBest)
        return weightKg > priorBest
    }

    fun weightStepKg(refWeightKg: Float): Float = when {
        refWeightKg < 20f -> 1f
        refWeightKg < 60f -> 2f
        else -> 2.5f
    }

    fun prFlagsForOrderedSets(
        sets: List<LoggedSet>,
        historicalBestKg: Float,
    ): List<Boolean> {
        var best = historicalBestKg
        return sets.map { set ->
            if (set.isWarmup) {
                false
            } else {
                val isPr = set.weightKg > best
                best = maxOf(best, set.weightKg)
                isPr
            }
        }
    }

    fun weightTickStepKg(range: Float): Float = when {
        range > 30f -> 20f
        range > 15f -> 10f
        range > 7.5f -> 5f
        else -> 2.5f
    }
}
