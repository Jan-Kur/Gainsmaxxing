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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `strength_pr_exercises` (
                `name` TEXT NOT NULL,
                `sortOrder` INTEGER NOT NULL,
                PRIMARY KEY(`name`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT OR IGNORE INTO `strength_pr_exercises` (`name`, `sortOrder`)
            SELECT `exerciseName`, `sortOrder` FROM `strength_pr_selection`
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT OR IGNORE INTO `strength_pr_exercises` (`name`, `sortOrder`)
            SELECT `exerciseName`, 1000 + MIN(`id`) FROM `strength_pr_entries`
            WHERE `exerciseName` NOT IN (SELECT `name` FROM `strength_pr_exercises`)
            GROUP BY `exerciseName`
            """.trimIndent(),
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `activity_types` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `colorPaletteIndex` INTEGER NOT NULL,
                `iconKey` TEXT NOT NULL,
                `sortOrder` INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `calendar_template_slots` (
                `dayOfWeek` INTEGER NOT NULL,
                `slot` TEXT NOT NULL,
                `activityTypeId` INTEGER,
                PRIMARY KEY(`dayOfWeek`, `slot`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `calendar_skip_overrides` (
                `date` TEXT NOT NULL,
                `slot` TEXT NOT NULL,
                PRIMARY KEY(`date`, `slot`)
            )
            """.trimIndent(),
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `activity_types` ADD COLUMN `customColorArgb` INTEGER",
        )
    }
}
