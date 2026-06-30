package com.gainsmaxxing.domain

import com.gainsmaxxing.domain.model.WeightUnit
import kotlin.math.roundToInt

object WeightFormat {
    private const val LBS_PER_KG = 2.20462f
    const val STEP_KG = 2.5f

    fun kgToDisplay(kg: Float, unit: WeightUnit): Float = when (unit) {
        WeightUnit.KG -> kg
        WeightUnit.LBS -> kg * LBS_PER_KG
    }

    fun displayToKg(display: Float, unit: WeightUnit): Float = when (unit) {
        WeightUnit.KG -> display
        WeightUnit.LBS -> display / LBS_PER_KG
    }

    fun formatWeight(kg: Float, unit: WeightUnit): String {
        val value = kgToDisplay(kg, unit)
        return if ((value * 10).roundToInt() % 10 == 0) {
            "${value.toInt()}"
        } else {
            "%.1f".format(value)
        }
    }

    fun unitLabel(unit: WeightUnit): String = when (unit) {
        WeightUnit.KG -> "kg"
        WeightUnit.LBS -> "lbs"
    }
}
