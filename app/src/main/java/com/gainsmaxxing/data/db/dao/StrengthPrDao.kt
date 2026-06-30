package com.gainsmaxxing.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.gainsmaxxing.data.db.entities.StrengthPrEntryEntity
import com.gainsmaxxing.data.db.entities.StrengthPrExerciseEntity
import com.gainsmaxxing.data.db.entities.StrengthPrSelectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StrengthPrDao {
    @Query("SELECT * FROM strength_pr_exercises ORDER BY sortOrder ASC")
    fun observeCatalog(): Flow<List<StrengthPrExerciseEntity>>

    @Query("SELECT * FROM strength_pr_exercises ORDER BY sortOrder ASC")
    suspend fun getCatalog(): List<StrengthPrExerciseEntity>

    @Query("SELECT * FROM strength_pr_selection ORDER BY sortOrder ASC")
    fun observeSelection(): Flow<List<StrengthPrSelectionEntity>>

    @Query("SELECT * FROM strength_pr_selection ORDER BY sortOrder ASC")
    suspend fun getSelection(): List<StrengthPrSelectionEntity>

    @Query("SELECT * FROM strength_pr_entries WHERE exerciseName = :exerciseName ORDER BY loggedAtEpochMs DESC")
    fun observeEntriesForExercise(exerciseName: String): Flow<List<StrengthPrEntryEntity>>

    @Query("SELECT * FROM strength_pr_entries ORDER BY loggedAtEpochMs DESC")
    fun observeAllEntries(): Flow<List<StrengthPrEntryEntity>>

    @Query("SELECT * FROM strength_pr_entries ORDER BY loggedAtEpochMs ASC")
    suspend fun getAllEntries(): List<StrengthPrEntryEntity>

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM strength_pr_exercises")
    suspend fun maxCatalogSortOrder(): Int

    @Query("SELECT name FROM strength_pr_exercises")
    suspend fun getAllCatalogNames(): List<String>

    @Insert
    suspend fun insertEntry(entry: StrengthPrEntryEntity): Long

    @Insert
    suspend fun insertCatalogExercise(item: StrengthPrExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCatalogExercises(items: List<StrengthPrExerciseEntity>)

    @Insert
    suspend fun insertSelection(item: StrengthPrSelectionEntity)

    @Query("DELETE FROM strength_pr_selection")
    suspend fun deleteAllSelection()

    @Query("DELETE FROM strength_pr_selection WHERE exerciseName = :exerciseName")
    suspend fun deleteSelectionForExercise(exerciseName: String)

    @Query("DELETE FROM strength_pr_entries WHERE exerciseName = :exerciseName")
    suspend fun deleteEntriesForExercise(exerciseName: String)

    @Query("DELETE FROM strength_pr_exercises WHERE name = :exerciseName")
    suspend fun deleteCatalogExercise(exerciseName: String)

    @Transaction
    suspend fun replaceSelection(items: List<StrengthPrSelectionEntity>) {
        deleteAllSelection()
        items.forEach { insertSelection(it) }
    }

    @Transaction
    suspend fun deleteExerciseCompletely(exerciseName: String) {
        deleteEntriesForExercise(exerciseName)
        deleteSelectionForExercise(exerciseName)
        deleteCatalogExercise(exerciseName)
    }
}
