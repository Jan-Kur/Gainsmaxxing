package com.gainsmaxxing.di

import android.content.Context
import androidx.room.Room
import com.gainsmaxxing.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "gainsmaxxing.db",
        ).build()

    @Provides
    fun provideExerciseDao(db: AppDatabase) = db.exerciseDao()

    @Provides
    fun provideSplitDao(db: AppDatabase) = db.splitDao()

    @Provides
    fun provideWorkoutSessionDao(db: AppDatabase) = db.workoutSessionDao()

    @Provides
    fun provideSessionSetDao(db: AppDatabase) = db.sessionSetDao()

    @Provides
    fun provideUserPreferencesDao(db: AppDatabase) = db.userPreferencesDao()
}
