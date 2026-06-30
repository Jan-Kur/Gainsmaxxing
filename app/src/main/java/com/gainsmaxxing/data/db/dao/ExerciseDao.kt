package com.gainsmaxxing.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gainsmaxxing.data.db.entities.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: Long): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): ExerciseEntity?

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun observeAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    suspend fun getAll(): List<ExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(exercise: ExerciseEntity): Long
}
