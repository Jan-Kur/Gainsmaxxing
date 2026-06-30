package com.gainsmaxxing.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `bodyweight_entries` (
                `date` TEXT NOT NULL,
                `weightKg` REAL NOT NULL,
                PRIMARY KEY(`date`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `sleep_entries` (
                `date` TEXT NOT NULL,
                `hours` REAL NOT NULL,
                `energyTag` TEXT NOT NULL,
                PRIMARY KEY(`date`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `strength_pr_entries` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `exerciseName` TEXT NOT NULL,
                `oneRmKg` REAL NOT NULL,
                `loggedAtEpochMs` INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_strength_pr_entries_exerciseName` ON `strength_pr_entries` (`exerciseName`)",
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `strength_pr_selection` (
                `exerciseName` TEXT NOT NULL,
                `sortOrder` INTEGER NOT NULL,
                PRIMARY KEY(`exerciseName`)
            )
            """.trimIndent(),
        )
    }
}
