package com.gainsmaxxing.data.repository

import com.gainsmaxxing.data.db.dao.StrengthPrDao
import com.gainsmaxxing.data.db.entities.StrengthPrEntryEntity
import com.gainsmaxxing.data.db.entities.StrengthPrExerciseEntity
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

data class StrengthPrSettings(
    val catalog: List<String>,
    val selected: List<String>,
)

@Singleton
class StrengthPrRepository @Inject constructor(
    private val strengthPrDao: StrengthPrDao,
) {
    fun observeCatalog(): Flow<List<String>> =
        strengthPrDao.observeCatalog().map { rows ->
            rows.map { it.name }
        }

    fun observeSelection(): Flow<List<String>> =
        strengthPrDao.observeSelection().map { rows ->
            rows.map { it.exerciseName }
        }

    fun observeSettings(): Flow<StrengthPrSettings> =
        combine(observeCatalog(), observeSelection()) { catalog, selected ->
            StrengthPrSettings(catalog = catalog, selected = selected)
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

    suspend fun addExercise(name: String) {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Exercise name cannot be blank" }
        val catalog = strengthPrDao.getAllCatalogNames()
        require(trimmed !in catalog) { "Exercise already exists" }
        strengthPrDao.insertCatalogExercise(
            StrengthPrExerciseEntity(
                name = trimmed,
                sortOrder = strengthPrDao.maxCatalogSortOrder() + 1,
            ),
        )
    }

    suspend fun deleteExercise(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        strengthPrDao.deleteExerciseCompletely(trimmed)
    }

    suspend fun saveCatalogOrder(catalog: List<String>) {
        val existing = strengthPrDao.getAllCatalogNames().toSet()
        val entities = catalog
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && it in existing }
            .distinct()
            .mapIndexed { index, name ->
                StrengthPrExerciseEntity(name = name, sortOrder = index)
            }
            .toList()
        if (entities.isNotEmpty()) {
            strengthPrDao.upsertCatalogExercises(entities)
        }
    }

    suspend fun syncCatalog(previousCatalog: List<String>, nextCatalog: List<String>) {
        val previous = previousCatalog.toSet()
        val next = nextCatalog
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .toList()
        val nextSet = next.toSet()
        (previous - nextSet).forEach { deleteExercise(it) }
        val added = nextSet - previous
        added.forEach { addExercise(it) }
    }

    suspend fun saveSelection(exerciseNames: List<String>) {
        val catalog = strengthPrDao.getAllCatalogNames().toSet()
        val trimmed = exerciseNames
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && it in catalog }
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
