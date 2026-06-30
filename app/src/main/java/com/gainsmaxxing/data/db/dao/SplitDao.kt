package com.gainsmaxxing.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.gainsmaxxing.data.db.entities.SplitDayEntity
import com.gainsmaxxing.data.db.entities.TemplateExerciseEntity
import kotlinx.coroutines.flow.Flow

data class TemplateExerciseWithExercise(
    val id: Long,
    val dayOfWeek: Int,
    val exerciseId: Long,
    val sortOrder: Int,
    val targetSets: Int,
    val targetReps: Int,
    val exerciseName: String,
    val isBodyweight: Boolean,
)

@Dao
interface SplitDao {
    @Query("SELECT * FROM split_days ORDER BY dayOfWeek ASC")
    fun observeSplitDays(): Flow<List<SplitDayEntity>>

    @Query("SELECT * FROM split_days ORDER BY dayOfWeek ASC")
    suspend fun getSplitDays(): List<SplitDayEntity>

    @Query(
        """
        SELECT te.id, te.dayOfWeek, te.exerciseId, te.sortOrder, te.targetSets, te.targetReps,
               e.name AS exerciseName, e.isBodyweight AS isBodyweight
        FROM template_exercises te
        INNER JOIN exercises e ON e.id = te.exerciseId
        ORDER BY te.dayOfWeek ASC, te.sortOrder ASC
        """,
    )
    fun observeTemplateExercises(): Flow<List<TemplateExerciseWithExercise>>

    @Query("SELECT * FROM template_exercises ORDER BY dayOfWeek ASC, sortOrder ASC")
    suspend fun getTemplateExercises(): List<TemplateExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSplitDay(splitDay: SplitDayEntity)

    @Query("DELETE FROM template_exercises WHERE dayOfWeek = :dayOfWeek")
    suspend fun deleteTemplateExercisesForDay(dayOfWeek: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateExercises(exercises: List<TemplateExerciseEntity>)

    @Transaction
    suspend fun replaceDay(
        splitDay: SplitDayEntity,
        exercises: List<TemplateExerciseEntity>,
    ) {
        upsertSplitDay(splitDay)
        deleteTemplateExercisesForDay(splitDay.dayOfWeek)
        if (exercises.isNotEmpty()) {
            insertTemplateExercises(exercises)
        }
    }
}
