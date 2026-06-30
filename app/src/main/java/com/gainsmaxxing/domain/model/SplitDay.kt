package com.gainsmaxxing.domain.model

data class SplitDay(
    val dayOfWeek: Int,
    val workoutName: String?,
    val exercises: List<TemplateExercise>,
) {
    val isRestDay: Boolean get() = workoutName == null || exercises.isEmpty()
}
