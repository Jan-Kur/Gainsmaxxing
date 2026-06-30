package com.gainsmaxxing.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gainsmaxxing.data.db.entities.BodyweightEntryEntity
import com.gainsmaxxing.data.db.entities.SleepEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyMetricsDao {
    @Query("SELECT * FROM bodyweight_entries WHERE date >= :fromDate ORDER BY date ASC")
    fun observeBodyweightSince(fromDate: String): Flow<List<BodyweightEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBodyweight(entry: BodyweightEntryEntity)

    @Query("SELECT * FROM sleep_entries WHERE date >= :fromDate ORDER BY date ASC")
    fun observeSleepSince(fromDate: String): Flow<List<SleepEntryEntity>>

    @Query("SELECT * FROM sleep_entries ORDER BY date ASC")
    fun observeAllSleep(): Flow<List<SleepEntryEntity>>

    @Query("SELECT * FROM bodyweight_entries ORDER BY date ASC")
    suspend fun getAllBodyweight(): List<BodyweightEntryEntity>

    @Query("SELECT * FROM sleep_entries ORDER BY date ASC")
    suspend fun getAllSleep(): List<SleepEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSleep(entry: SleepEntryEntity)
}
