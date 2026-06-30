package com.gainsmaxxing.domain.model

data class LoggedSet(
    val id: Long = 0,
    val exerciseId: Long,
    val weightKg: Float,
    val reps: Int,
    val isWarmup: Boolean,
    val sortOrder: Int = 0,
)
