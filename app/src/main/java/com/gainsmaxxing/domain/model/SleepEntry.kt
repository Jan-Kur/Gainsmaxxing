package com.gainsmaxxing.domain.model

import java.time.LocalDate

data class SleepEntry(
    val date: LocalDate,
    val hours: Float,
    val energyTag: EnergyTag,
)
