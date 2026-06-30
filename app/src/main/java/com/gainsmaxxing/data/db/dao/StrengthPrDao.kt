package com.gainsmaxxing.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.gainsmaxxing.data.db.entities.StrengthPrEntryEntity
import com.gainsmaxxing.data.db.entities.StrengthPrSelectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StrengthPrDao {
    @Query("SELECT * FROM strength_pr_selection ORDER BY sortOrder ASC")
    fun observeSelection(): Flow<List<StrengthPrSelectionEntity>>

    @Query("SELECT * FROM strength_pr_entries WHERE exerciseName = :exerciseName ORDER BY loggedAtEpochMs DESC")
    fun observeEntriesForExercise(exerciseName: String): Flow<List<StrengthPrEntryEntity>>

    @Query("SELECT * FROM strength_pr_entries ORDER BY loggedAtEpochMs DESC")
    fun observeAllEntries(): Flow<List<StrengthPrEntryEntity>>

    @Insert
    suspend fun insertEntry(entry: StrengthPrEntryEntity): Long

    @Insert
    suspend fun insertSelection(item: StrengthPrSelectionEntity)

    @Query("DELETE FROM strength_pr_selection")
    suspend fun deleteAllSelection()

    @Transaction
    suspend fun replaceSelection(items: List<StrengthPrSelectionEntity>) {
        deleteAllSelection()
        items.forEach { insertSelection(it) }
    }
}
