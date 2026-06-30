package com.gainsmaxxing.data.repository

import com.gainsmaxxing.data.db.dao.BodyMetricsDao
import com.gainsmaxxing.data.db.dao.CalendarDao
import com.gainsmaxxing.data.db.dao.ExerciseDao
import com.gainsmaxxing.data.db.dao.ImportDao
import com.gainsmaxxing.data.db.dao.SessionSetDao
import com.gainsmaxxing.data.db.dao.SplitDao
import com.gainsmaxxing.data.db.dao.StrengthPrDao
import com.gainsmaxxing.data.db.dao.UserPreferencesDao
import com.gainsmaxxing.data.db.dao.WorkoutSessionDao
import com.gainsmaxxing.data.export.ExportCodec
import com.gainsmaxxing.data.export.ExportData
import com.gainsmaxxing.data.export.GainsmaxxingExport
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

data class ExportPayload(
    val fileName: String,
    val json: String,
)

data class BackupMessage(
    val text: String,
    val isError: Boolean = false,
)

@Singleton
class ExportRepository @Inject constructor(
    private val importDao: ImportDao,
    private val userPreferencesDao: UserPreferencesDao,
    private val calendarDao: CalendarDao,
    private val exerciseDao: ExerciseDao,
    private val splitDao: SplitDao,
    private val workoutSessionDao: WorkoutSessionDao,
    private val sessionSetDao: SessionSetDao,
    private val bodyMetricsDao: BodyMetricsDao,
    private val strengthPrDao: StrengthPrDao,
) {
    suspend fun createExport(now: Instant = Instant.now()): ExportPayload {
        val export = GainsmaxxingExport(
            formatVersion = ExportCodec.FORMAT_VERSION,
            exportedAt = now.toString(),
            data = ExportData(
                userPreferences = userPreferencesDao.get(),
                activityTypes = calendarDao.getActivityTypes(),
                calendarTemplateSlots = calendarDao.getTemplateSlots(),
                calendarSkips = calendarDao.getAllSkips(),
                exercises = exerciseDao.getAll(),
                splitDays = splitDao.getSplitDays(),
                templateExercises = splitDao.getTemplateExercises(),
                workoutSessions = workoutSessionDao.getAll(),
                sessionSets = sessionSetDao.getAll(),
                bodyweightEntries = bodyMetricsDao.getAllBodyweight(),
                sleepEntries = bodyMetricsDao.getAllSleep(),
                strengthPrExercises = strengthPrDao.getCatalog(),
                strengthPrSelection = strengthPrDao.getSelection(),
                strengthPrEntries = strengthPrDao.getAllEntries(),
            ),
        )
        return ExportPayload(
            fileName = ExportCodec.buildFileName(now),
            json = ExportCodec.encode(export),
        )
    }

    suspend fun importFromJson(json: String) {
        val export = ExportCodec.decode(json)
        ExportCodec.validate(export)
        importDao.replaceAll(export.data)
    }
}
