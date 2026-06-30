package com.gainsmaxxing.data.mapper

import com.gainsmaxxing.data.db.dao.TemplateExerciseWithExercise
import com.gainsmaxxing.data.db.entities.ExerciseEntity
import com.gainsmaxxing.data.db.entities.SessionSetEntity
import com.gainsmaxxing.data.db.entities.SplitDayEntity
import com.gainsmaxxing.data.db.entities.UserPreferencesEntity
import com.gainsmaxxing.data.db.entities.WorkoutSessionEntity
import com.gainsmaxxing.domain.model.Exercise
import com.gainsmaxxing.domain.model.LastExerciseReference
import com.gainsmaxxing.domain.model.LoggedSet
import com.gainsmaxxing.domain.model.SplitDay
import com.gainsmaxxing.domain.model.TemplateExercise
import com.gainsmaxxing.domain.model.UserProfile
import com.gainsmaxxing.domain.model.WeightUnit
import com.gainsmaxxing.domain.model.WorkoutSession
import com.gainsmaxxing.domain.model.WorkoutStatus
import java.time.Instant

fun ExerciseEntity.toDomain(): Exercise = Exercise(
    id = id,
    name = name,
    isBodyweight = isBodyweight,
)

fun SessionSetEntity.toDomain(): LoggedSet = LoggedSet(
    id = id,
    exerciseId = exerciseId,
    weightKg = weightKg,
    reps = reps,
    isWarmup = isWarmup,
    sortOrder = sortOrder,
)

fun WorkoutSessionEntity.toDomain(sets: List<LoggedSet>): WorkoutSession = WorkoutSession(
    id = id,
    dayOfWeek = dayOfWeek,
    workoutName = workoutName,
    status = WorkoutStatus.valueOf(status),
    startedAt = Instant.ofEpochMilli(startedAtEpochMs),
    finishedAt = finishedAtEpochMs?.let(Instant::ofEpochMilli),
    sets = sets,
)

fun UserPreferencesEntity.toDomain(): UserProfile = UserProfile(
    name = profileName,
    weightUnit = WeightUnit.valueOf(weightUnit),
)

fun UserProfile.toEntity(): UserPreferencesEntity = UserPreferencesEntity(
    profileName = name,
    weightUnit = weightUnit.name,
)

fun buildSplitDays(
    splitDayEntities: List<SplitDayEntity>,
    templateRows: List<TemplateExerciseWithExercise>,
    lastReferences: Map<Long, LastExerciseReference>,
): List<SplitDay> {
    val templatesByDay = templateRows.groupBy { it.dayOfWeek }
    val configuredDays = splitDayEntities.associateBy { it.dayOfWeek }

    return (0..6).map { dayOfWeek ->
        val splitDay = configuredDays[dayOfWeek]
        val exercises = templatesByDay[dayOfWeek].orEmpty().map { row ->
            TemplateExercise(
                templateId = row.id,
                exercise = Exercise(
                    id = row.exerciseId,
                    name = row.exerciseName,
                    isBodyweight = row.isBodyweight,
                ),
                sortOrder = row.sortOrder,
                targetSets = row.targetSets,
                targetReps = row.targetReps,
                lastReference = lastReferences[row.exerciseId],
            )
        }
        SplitDay(
            dayOfWeek = dayOfWeek,
            workoutName = splitDay?.workoutName,
            exercises = exercises,
        )
    }
}
