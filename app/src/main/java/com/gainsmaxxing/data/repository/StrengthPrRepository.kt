package com.gainsmaxxing.data.repository

import com.gainsmaxxing.data.db.dao.StrengthPrDao
import com.gainsmaxxing.data.db.entities.StrengthPrEntryEntity
import com.gainsmaxxing.data.db.entities.StrengthPrSelectionEntity
import com.gainsmaxxing.data.mapper.toDomain
import com.gainsmaxxing.domain.model.StrengthPrEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

data class StrengthPrSummary(
    val exerciseName: String,
    val entries: List<StrengthPrEntry>,
)

@Singleton
class StrengthPrRepository @Inject constructor(
    private val strengthPrDao: StrengthPrDao,
) {
    fun observeSelection(): Flow<List<String>> =
        strengthPrDao.observeSelection().map { rows ->
            rows.map { it.exerciseName }
        }

    fun observeSummaries(): Flow<List<StrengthPrSummary>> =
        combine(
            strengthPrDao.observeSelection(),
            strengthPrDao.observeAllEntries(),
        ) { selection, allEntries ->
            val byExercise = allEntries
                .map { it.toDomain() }
                .groupBy { it.exerciseName }
            selection.map { row ->
                StrengthPrSummary(
                    exerciseName = row.exerciseName,
                    entries = byExercise[row.exerciseName].orEmpty(),
                )
            }
        }

    fun observeEntriesForExercise(exerciseName: String): Flow<List<StrengthPrEntry>> =
        strengthPrDao.observeEntriesForExercise(exerciseName).map { rows ->
            rows.map { it.toDomain() }
        }

    suspend fun logOneRm(exerciseName: String, oneRmKg: Float) {
        val trimmed = exerciseName.trim()
        require(trimmed.isNotEmpty()) { "Exercise name cannot be blank" }
        require(oneRmKg > 0f) { "1RM must be positive" }
        strengthPrDao.insertEntry(
            StrengthPrEntryEntity(
                exerciseName = trimmed,
                oneRmKg = oneRmKg,
                loggedAtEpochMs = Instant.now().toEpochMilli(),
            ),
        )
    }

    suspend fun saveSelection(exerciseNames: List<String>) {
        val trimmed = exerciseNames
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(MAX_SELECTION)
            .toList()
        val entities = trimmed.mapIndexed { index, name ->
            StrengthPrSelectionEntity(exerciseName = name, sortOrder = index)
        }
        strengthPrDao.replaceSelection(entities)
    }

    companion object {
        const val MAX_SELECTION = 4
    }
}
