package com.gainsmaxxing.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gainsmaxxing.data.db.entities.SessionSetEntity
import com.gainsmaxxing.data.db.entities.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {
    @Query("SELECT * FROM workout_sessions WHERE status = 'IN_PROGRESS' LIMIT 1")
    fun observeInProgress(): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM workout_sessions WHERE status = 'IN_PROGRESS' LIMIT 1")
    suspend fun getInProgress(): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getById(sessionId: Long): WorkoutSessionEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(session: WorkoutSessionEntity): Long

    @Query(
        """
        UPDATE workout_sessions
        SET status = 'COMPLETED', finishedAtEpochMs = :finishedAtEpochMs
        WHERE id = :sessionId
        """,
    )
    suspend fun markCompleted(sessionId: Long, finishedAtEpochMs: Long)

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun delete(sessionId: Long)

    @Query(
        """
        SELECT * FROM workout_sessions
        WHERE status = 'COMPLETED'
          AND id IN (SELECT sessionId FROM session_sets WHERE exerciseId = :exerciseId)
        ORDER BY startedAtEpochMs DESC
        """,
    )
    suspend fun getCompletedSessionsForExercise(exerciseId: Long): List<WorkoutSessionEntity>
}

@Dao
interface SessionSetDao {
    @Query("SELECT * FROM session_sets WHERE sessionId = :sessionId ORDER BY sortOrder ASC")
    fun observeForSession(sessionId: Long): Flow<List<SessionSetEntity>>

    @Query("SELECT * FROM session_sets WHERE sessionId = :sessionId ORDER BY sortOrder ASC")
    suspend fun getForSession(sessionId: Long): List<SessionSetEntity>

    @Query(
        """
        SELECT * FROM session_sets
        WHERE exerciseId = :exerciseId
          AND sessionId IN (
            SELECT id FROM workout_sessions
            WHERE status = 'COMPLETED' AND id IN (
                SELECT sessionId FROM session_sets WHERE exerciseId = :exerciseId
            )
        )
        ORDER BY sessionId ASC, sortOrder ASC
        """
    )
    suspend fun getCompletedSetsForExercise(exerciseId: Long): List<SessionSetEntity>

    @Query(
        """
        SELECT MAX(ss.weightKg) FROM session_sets ss
        INNER JOIN workout_sessions ws ON ws.id = ss.sessionId
        WHERE ss.exerciseId = :exerciseId
          AND ws.status = 'COMPLETED'
          AND ss.isWarmup = 0
          AND ws.id != :excludeSessionId
        """,
    )
    suspend fun getHistoricalBestWeightKg(exerciseId: Long, excludeSessionId: Long): Float?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(set: SessionSetEntity): Long

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM session_sets WHERE sessionId = :sessionId")
    suspend fun maxSortOrder(sessionId: Long): Int
}
