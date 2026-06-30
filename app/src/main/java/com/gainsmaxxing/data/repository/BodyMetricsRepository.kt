package com.gainsmaxxing.data.repository

import com.gainsmaxxing.data.db.dao.BodyMetricsDao
import com.gainsmaxxing.data.mapper.toDomain
import com.gainsmaxxing.data.mapper.toEntity
import com.gainsmaxxing.domain.SleepChartSlots
import com.gainsmaxxing.domain.model.BodyweightEntry
import com.gainsmaxxing.domain.model.SleepEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BodyMetricsRepository @Inject constructor(
    private val bodyMetricsDao: BodyMetricsDao,
) {
    fun observeBodyweightWeeks(weeks: Int = BODYWEIGHT_WEEKS): Flow<List<BodyweightEntry>> {
        val fromDate = LocalDate.now().minusWeeks(weeks.toLong()).toString()
        return bodyMetricsDao.observeBodyweightSince(fromDate).map { rows ->
            rows.map { it.toDomain() }
        }
    }

    fun observeSleepChart(): Flow<List<SleepEntry>> =
        bodyMetricsDao.observeAllSleep().map { rows ->
            SleepChartSlots.build(rows.map { it.toDomain() })
        }

    suspend fun logBodyweight(date: LocalDate, weightKg: Float) {
        require(weightKg > 0f) { "Weight must be positive" }
        bodyMetricsDao.upsertBodyweight(
            BodyweightEntry(date = date, weightKg = weightKg).toEntity(),
        )
    }

    suspend fun logSleep(entry: SleepEntry) {
        require(entry.hours >= 1f / 60f) { "Sleep duration must be at least 1 minute" }
        bodyMetricsDao.upsertSleep(entry.toEntity())
    }

    companion object {
        const val BODYWEIGHT_WEEKS = 26
        const val SLEEP_DAYS = SleepChartSlots.SLOT_COUNT
    }
}
