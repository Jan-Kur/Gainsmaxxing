package com.gainsmaxxing.data.repository

import com.gainsmaxxing.data.db.dao.ExerciseDao
import com.gainsmaxxing.data.db.dao.SessionSetDao
import com.gainsmaxxing.data.db.dao.SplitDao
import com.gainsmaxxing.data.db.dao.WorkoutSessionDao
import com.gainsmaxxing.data.db.entities.ExerciseEntity
import com.gainsmaxxing.data.db.entities.SessionSetEntity
import com.gainsmaxxing.data.db.entities.SplitDayEntity
import com.gainsmaxxing.data.db.entities.TemplateExerciseEntity
import com.gainsmaxxing.data.db.entities.WorkoutSessionEntity
import com.gainsmaxxing.data.mapper.buildSplitDays
import com.gainsmaxxing.data.mapper.toDomain
import com.gainsmaxxing.domain.model.Exercise
import com.gainsmaxxing.domain.model.LastExerciseReference
import com.gainsmaxxing.domain.model.LoggedSet
import com.gainsmaxxing.domain.model.SplitDay
import com.gainsmaxxing.domain.model.WorkoutSession
import com.gainsmaxxing.domain.model.WorkoutStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutRepository @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val splitDao: SplitDao,
    private val workoutSessionDao: WorkoutSessionDao,
    private val sessionSetDao: SessionSetDao,
) {
    fun observeSplit(): Flow<List<SplitDay>> =
        combine(
            splitDao.observeSplitDays(),
            splitDao.observeTemplateExercises(),
        ) { splitDays, templates ->
            splitDays to templates
        }.flatMapLatest { (splitDays, templates) ->
            flow {
                val exerciseIds = templates.map { it.exerciseId }.distinct()
                val lastReferences = exerciseIds.mapNotNull { exerciseId ->
                    getLastSessionReference(exerciseId)?.let { exerciseId to it }
                }.toMap()
                emit(buildSplitDays(splitDays, templates, lastReferences))
            }
        }

    fun observeInProgressSession(): Flow<WorkoutSession?> =
        workoutSessionDao.observeInProgress().flatMapLatest { sessionEntity ->
            if (sessionEntity == null) {
                flowOf(null)
            } else {
                sessionSetDao.observeForSession(sessionEntity.id).map { sets ->
                    sessionEntity.toDomain(sets.map { it.toDomain() })
                }
            }
        }

    suspend fun getOrCreateExercise(name: String, isBodyweight: Boolean): Exercise {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Exercise name cannot be blank" }
        val existing = exerciseDao.findByName(trimmed)
        if (existing != null) {
            if (existing.isBodyweight != isBodyweight) {
                // Keep first definition; bodyweight flag is set on creation.
            }
            return existing.toDomain()
        }
        val id = exerciseDao.insert(
            ExerciseEntity(name = trimmed, isBodyweight = isBodyweight),
        )
        return Exercise(id = id, name = trimmed, isBodyweight = isBodyweight)
    }

    suspend fun saveSplitDay(splitDay: SplitDay) {
        if (splitDay.isRestDay) {
            splitDao.replaceDay(
                splitDay = SplitDayEntity(dayOfWeek = splitDay.dayOfWeek, workoutName = null),
                exercises = emptyList(),
            )
            return
        }

        val workoutName = splitDay.workoutName?.trim().orEmpty()
        require(workoutName.isNotEmpty()) { "Workout name cannot be blank" }

        val templateEntities = splitDay.exercises.mapIndexed { index, template ->
            TemplateExerciseEntity(
                dayOfWeek = splitDay.dayOfWeek,
                exerciseId = template.exercise.id,
                sortOrder = index,
                targetSets = template.targetSets,
                targetReps = template.targetReps,
            )
        }

        splitDao.replaceDay(
            splitDay = SplitDayEntity(
                dayOfWeek = splitDay.dayOfWeek,
                workoutName = workoutName,
            ),
            exercises = templateEntities,
        )
    }

    suspend fun startWorkout(dayOfWeek: Int, workoutName: String): Long {
        workoutSessionDao.getInProgress()?.let { workoutSessionDao.delete(it.id) }
        return workoutSessionDao.insert(
            WorkoutSessionEntity(
                dayOfWeek = dayOfWeek,
                workoutName = workoutName,
                status = WorkoutStatus.IN_PROGRESS.name,
                startedAtEpochMs = System.currentTimeMillis(),
                finishedAtEpochMs = null,
            ),
        )
    }

    suspend fun resumeInProgressSession(): WorkoutSession? {
        val entity = workoutSessionDao.getInProgress() ?: return null
        val sets = sessionSetDao.getForSession(entity.id).map { it.toDomain() }
        return entity.toDomain(sets)
    }

    suspend fun logSet(
        sessionId: Long,
        exerciseId: Long,
        weightKg: Float,
        reps: Int,
        isWarmup: Boolean,
    ) {
        val sortOrder = sessionSetDao.maxSortOrder(sessionId) + 1
        sessionSetDao.insert(
            SessionSetEntity(
                sessionId = sessionId,
                exerciseId = exerciseId,
                weightKg = weightKg,
                reps = reps,
                isWarmup = isWarmup,
                sortOrder = sortOrder,
            ),
        )
    }

    suspend fun finishWorkout(sessionId: Long) {
        workoutSessionDao.markCompleted(sessionId, System.currentTimeMillis())
    }

    suspend fun discardWorkout(sessionId: Long) {
        workoutSessionDao.delete(sessionId)
    }

    suspend fun getHistoricalBestWeightKg(exerciseId: Long, excludeSessionId: Long): Float =
        sessionSetDao.getHistoricalBestWeightKg(exerciseId, excludeSessionId) ?: 0f

    suspend fun getExerciseHistory(exerciseId: Long): List<WorkoutSession> {
        val sessions = workoutSessionDao.getCompletedSessionsForExercise(exerciseId)
        val setsBySession = sessionSetDao.getCompletedSetsForExercise(exerciseId)
            .groupBy { it.sessionId }

        return sessions.map { session ->
            session.toDomain(
                setsBySession[session.id].orEmpty()
                    .filter { it.exerciseId == exerciseId }
                    .map { it.toDomain() },
            )
        }
    }

    suspend fun getExercise(exerciseId: Long): Exercise? =
        exerciseDao.getById(exerciseId)?.toDomain()

    suspend fun getLastSessionReference(exerciseId: Long): LastExerciseReference? {
        val lastSession = workoutSessionDao.getCompletedSessionsForExercise(exerciseId).firstOrNull()
            ?: return null
        val workingSets = sessionSetDao.getForSession(lastSession.id)
            .filter { it.exerciseId == exerciseId && !it.isWarmup }
        if (workingSets.isEmpty()) return null
        val topSet = workingSets.maxWith(
            compareBy<SessionSetEntity> { it.weightKg }.thenBy { it.sortOrder },
        )
        return LastExerciseReference(
            workingSetCount = workingSets.size,
            topSetReps = topSet.reps,
            topSetWeightKg = topSet.weightKg,
        )
    }
}
