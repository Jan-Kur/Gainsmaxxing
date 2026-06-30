package com.gainsmaxxing.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gainsmaxxing.data.db.dao.BodyMetricsDao
import com.gainsmaxxing.data.db.dao.ExerciseDao
import com.gainsmaxxing.data.db.dao.SessionSetDao
import com.gainsmaxxing.data.db.dao.SplitDao
import com.gainsmaxxing.data.db.dao.StrengthPrDao
import com.gainsmaxxing.data.db.dao.UserPreferencesDao
import com.gainsmaxxing.data.db.dao.WorkoutSessionDao
import com.gainsmaxxing.data.db.entities.BodyweightEntryEntity
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

@Database(
    entities = [
        ExerciseEntity::class,
        SplitDayEntity::class,
        TemplateExerciseEntity::class,
        WorkoutSessionEntity::class,
        SessionSetEntity::class,
        UserPreferencesEntity::class,
        BodyweightEntryEntity::class,
        SleepEntryEntity::class,
        StrengthPrEntryEntity::class,
        StrengthPrExerciseEntity::class,
        StrengthPrSelectionEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun splitDao(): SplitDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun sessionSetDao(): SessionSetDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun bodyMetricsDao(): BodyMetricsDao
    abstract fun strengthPrDao(): StrengthPrDao
}
