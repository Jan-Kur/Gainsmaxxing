package com.gainsmaxxing.domain.model

data class TemplateExercise(
    val templateId: Long,
    val exercise: Exercise,
    val sortOrder: Int,
    val targetSets: Int,
    val targetReps: Int,
    val lastReference: LastExerciseReference? = null,
) {
    val refWeightKg: Float? get() = lastReference?.topSetWeightKg
}
