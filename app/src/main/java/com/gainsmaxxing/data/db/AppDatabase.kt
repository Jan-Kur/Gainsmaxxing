package com.gainsmaxxing.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gainsmaxxing.data.db.dao.ExerciseDao
import com.gainsmaxxing.data.db.dao.SessionSetDao
import com.gainsmaxxing.data.db.dao.SplitDao
import com.gainsmaxxing.data.db.dao.UserPreferencesDao
import com.gainsmaxxing.data.db.dao.WorkoutSessionDao
import com.gainsmaxxing.data.db.entities.ExerciseEntity
import com.gainsmaxxing.data.db.entities.SessionSetEntity
import com.gainsmaxxing.data.db.entities.SplitDayEntity
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
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun splitDao(): SplitDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun sessionSetDao(): SessionSetDao
    abstract fun userPreferencesDao(): UserPreferencesDao
}
