package com.gainsmaxxing.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class WorkoutSession(
    val id: Long,
    val dayOfWeek: Int,
    val workoutName: String,
    val status: WorkoutStatus,
    val startedAt: Instant,
    val finishedAt: Instant?,
    val sets: List<LoggedSet>,
) {
    val date: LocalDate
        get() = startedAt.atZone(ZoneId.systemDefault()).toLocalDate()

    fun setsForExercise(exerciseId: Long): List<LoggedSet> =
        sets.filter { it.exerciseId == exerciseId }
}
