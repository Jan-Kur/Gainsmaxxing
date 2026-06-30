package com.gainsmaxxing.domain

import com.gainsmaxxing.domain.model.StrengthPrEntry
import com.gainsmaxxing.domain.model.WeightUnit
import java.util.Locale
import kotlin.math.roundToInt

object StrengthPrComparison {
    fun latestEntry(entries: List<StrengthPrEntry>): StrengthPrEntry? =
        entries.maxByOrNull { it.loggedAt }

    fun previousEntry(entries: List<StrengthPrEntry>, latest: StrengthPrEntry): StrengthPrEntry? =
        entries
            .filter { it.id != latest.id }
            .maxByOrNull { it.loggedAt }

    fun formatDeltaKg(
        currentKg: Float,
        previousKg: Float?,
        unit: WeightUnit,
    ): String {
        if (previousKg == null) return "—"
        val deltaKg = currentKg - previousKg
        val deltaDisplay = WeightFormat.kgToDisplay(deltaKg, unit)
        val rounded = (deltaDisplay * 10).roundToInt() / 10f
        if (rounded == 0f) return "—"
        val unitLabel = WeightFormat.unitLabel(unit)
        val sign = if (rounded > 0f) "+" else ""
        val valueStr = if ((rounded * 10).roundToInt() % 10 == 0) {
            "${rounded.toInt()}"
        } else {
            "%.1f".format(Locale.ROOT, rounded)
        }
        return "$sign$valueStr $unitLabel"
    }
}
