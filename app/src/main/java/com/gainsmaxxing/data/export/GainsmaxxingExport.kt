package com.gainsmaxxing.data.export

import com.gainsmaxxing.data.db.entities.ActivityTypeEntity
import com.gainsmaxxing.data.db.entities.BodyweightEntryEntity
import com.gainsmaxxing.data.db.entities.CalendarSkipEntity
import com.gainsmaxxing.data.db.entities.CalendarTemplateSlotEntity
import com.gainsmaxxing.data.db.entities.ExerciseEntity
import com.gainsmaxxing.data.db.entities.SessionSetEntity
import com.gainsmaxxing.data.db.entities.SleepEntryEntity
import com.gainsmaxxing.data.db.entities.SplitDayEntity
import com.gainsmaxxing.data.db.entities.StrengthPrEntryEntity
import com.gainsmaxxing.data.db.entities.StrengthPrExerciseEntity
import com.gainsmaxxing.data.db.entities.StrengthPrSelectionEntity
import com.gainsmaxxing.data.db.entities.TemplateExerciseEntity
import com.gainsmaxxing.data.db.entities.UserPreferencesEntity
import com.gainsmaxxing.data.db.entities.WorkoutSessionEntity

data class GainsmaxxingExport(
    val formatVersion: Int,
    val exportedAt: String,
    val data: ExportData,
)

data class ExportData(
    val userPreferences: UserPreferencesEntity?,
    val activityTypes: List<ActivityTypeEntity>,
    val calendarTemplateSlots: List<CalendarTemplateSlotEntity>,
    val calendarSkips: List<CalendarSkipEntity>,
    val exercises: List<ExerciseEntity>,
    val splitDays: List<SplitDayEntity>,
    val templateExercises: List<TemplateExerciseEntity>,
    val workoutSessions: List<WorkoutSessionEntity>,
    val sessionSets: List<SessionSetEntity>,
    val bodyweightEntries: List<BodyweightEntryEntity>,
    val sleepEntries: List<SleepEntryEntity>,
    val strengthPrExercises: List<StrengthPrExerciseEntity>,
    val strengthPrSelection: List<StrengthPrSelectionEntity>,
    val strengthPrEntries: List<StrengthPrEntryEntity>,
)
