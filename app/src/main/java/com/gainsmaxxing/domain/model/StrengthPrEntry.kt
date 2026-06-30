package com.gainsmaxxing.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class StrengthPrEntry(
    val id: Long,
    val exerciseName: String,
    val oneRmKg: Float,
    val loggedAt: Instant,
) {
    val loggedDate: LocalDate
        get() = loggedAt.atZone(ZoneId.systemDefault()).toLocalDate()
}
