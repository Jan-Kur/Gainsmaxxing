package com.gainsmaxxing.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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
import com.gainsmaxxing.data.export.ExportData

@Dao
abstract class ImportDao {
    @Query("DELETE FROM session_sets")
    abstract suspend fun deleteAllSessionSets()

    @Query("DELETE FROM workout_sessions")
    abstract suspend fun deleteAllWorkoutSessions()

    @Query("DELETE FROM template_exercises")
    abstract suspend fun deleteAllTemplateExercises()

    @Query("DELETE FROM calendar_template_slots")
    abstract suspend fun deleteAllCalendarTemplateSlots()

    @Query("DELETE FROM calendar_skip_overrides")
    abstract suspend fun deleteAllCalendarSkips()

    @Query("DELETE FROM split_days")
    abstract suspend fun deleteAllSplitDays()

    @Query("DELETE FROM exercises")
    abstract suspend fun deleteAllExercises()

    @Query("DELETE FROM activity_types")
    abstract suspend fun deleteAllActivityTypes()

    @Query("DELETE FROM bodyweight_entries")
    abstract suspend fun deleteAllBodyweightEntries()

    @Query("DELETE FROM sleep_entries")
    abstract suspend fun deleteAllSleepEntries()

    @Query("DELETE FROM strength_pr_entries")
    abstract suspend fun deleteAllStrengthPrEntries()

    @Query("DELETE FROM strength_pr_selection")
    abstract suspend fun deleteAllStrengthPrSelection()

    @Query("DELETE FROM strength_pr_exercises")
    abstract suspend fun deleteAllStrengthPrExercises()

    @Query("DELETE FROM user_preferences")
    abstract suspend fun deleteAllUserPreferences()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertUserPreferences(preferences: UserPreferencesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertActivityTypes(types: List<ActivityTypeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSplitDays(days: List<SplitDayEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertCalendarTemplateSlots(slots: List<CalendarTemplateSlotEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertCalendarSkips(skips: List<CalendarSkipEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTemplateExercises(exercises: List<TemplateExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertWorkoutSessions(sessions: List<WorkoutSessionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSessionSets(sets: List<SessionSetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertBodyweightEntries(entries: List<BodyweightEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSleepEntries(entries: List<SleepEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertStrengthPrExercises(exercises: List<StrengthPrExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertStrengthPrSelection(selection: List<StrengthPrSelectionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertStrengthPrEntries(entries: List<StrengthPrEntryEntity>)

    @Transaction
    open suspend fun replaceAll(data: ExportData) {
        deleteAllSessionSets()
        deleteAllWorkoutSessions()
        deleteAllTemplateExercises()
        deleteAllCalendarTemplateSlots()
        deleteAllCalendarSkips()
        deleteAllSplitDays()
        deleteAllExercises()
        deleteAllActivityTypes()
        deleteAllBodyweightEntries()
        deleteAllSleepEntries()
        deleteAllStrengthPrEntries()
        deleteAllStrengthPrSelection()
        deleteAllStrengthPrExercises()
        deleteAllUserPreferences()

        insertUserPreferences(
            data.userPreferences ?: UserPreferencesEntity(
                profileName = "Athlete",
                weightUnit = "KG",
            ),
        )
        if (data.exercises.isNotEmpty()) insertExercises(data.exercises)
        if (data.activityTypes.isNotEmpty()) insertActivityTypes(data.activityTypes)
        if (data.splitDays.isNotEmpty()) insertSplitDays(data.splitDays)
        if (data.calendarTemplateSlots.isNotEmpty()) {
            insertCalendarTemplateSlots(data.calendarTemplateSlots)
        }
        if (data.calendarSkips.isNotEmpty()) insertCalendarSkips(data.calendarSkips)
        if (data.templateExercises.isNotEmpty()) insertTemplateExercises(data.templateExercises)
        if (data.workoutSessions.isNotEmpty()) insertWorkoutSessions(data.workoutSessions)
        if (data.sessionSets.isNotEmpty()) insertSessionSets(data.sessionSets)
        if (data.bodyweightEntries.isNotEmpty()) insertBodyweightEntries(data.bodyweightEntries)
        if (data.sleepEntries.isNotEmpty()) insertSleepEntries(data.sleepEntries)
        if (data.strengthPrExercises.isNotEmpty()) insertStrengthPrExercises(data.strengthPrExercises)
        if (data.strengthPrSelection.isNotEmpty()) insertStrengthPrSelection(data.strengthPrSelection)
        if (data.strengthPrEntries.isNotEmpty()) insertStrengthPrEntries(data.strengthPrEntries)
    }
}
