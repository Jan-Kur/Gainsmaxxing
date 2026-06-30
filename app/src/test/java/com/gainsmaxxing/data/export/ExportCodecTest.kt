package com.gainsmaxxing.data.export

import com.gainsmaxxing.data.db.entities.BodyweightEntryEntity
import com.gainsmaxxing.data.db.entities.ExerciseEntity
import com.gainsmaxxing.data.db.entities.StrengthPrEntryEntity
import com.gainsmaxxing.data.db.entities.UserPreferencesEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class ExportCodecTest {
    @Test
    fun `round-trips export payload`() {
        val original = GainsmaxxingExport(
            formatVersion = ExportCodec.FORMAT_VERSION,
            exportedAt = Instant.parse("2026-06-30T12:00:00Z").toString(),
            data = ExportData(
                userPreferences = UserPreferencesEntity(
                    profileName = "Athlete",
                    weightUnit = "KG",
                ),
                activityTypes = emptyList(),
                calendarTemplateSlots = emptyList(),
                calendarSkips = emptyList(),
                exercises = listOf(
                    ExerciseEntity(id = 1, name = "Bench Press", isBodyweight = false),
                ),
                splitDays = emptyList(),
                templateExercises = emptyList(),
                workoutSessions = emptyList(),
                sessionSets = emptyList(),
                bodyweightEntries = listOf(
                    BodyweightEntryEntity(date = "2026-06-01", weightKg = 80f),
                ),
                sleepEntries = emptyList(),
                strengthPrExercises = emptyList(),
                strengthPrSelection = emptyList(),
                strengthPrEntries = listOf(
                    StrengthPrEntryEntity(
                        id = 1,
                        exerciseName = "Bench Press",
                        oneRmKg = 100f,
                        loggedAtEpochMs = 1_700_000_000_000,
                    ),
                ),
            ),
        )

        val json = ExportCodec.encode(original)
        val decoded = ExportCodec.decode(json)

        assertEquals(original, decoded)
    }

    @Test
    fun `builds dated backup filename`() {
        val fileName = ExportCodec.buildFileName(Instant.parse("2026-06-30T08:15:00Z"))
        assertEquals("gainsmaxxing-backup-2026-06-30.json", fileName)
    }
    @Test
    fun `rejects unsupported backup version`() {
        val export = GainsmaxxingExport(
            formatVersion = 99,
            exportedAt = Instant.parse("2026-06-30T12:00:00Z").toString(),
            data = ExportData(
                userPreferences = null,
                activityTypes = emptyList(),
                calendarTemplateSlots = emptyList(),
                calendarSkips = emptyList(),
                exercises = emptyList(),
                splitDays = emptyList(),
                templateExercises = emptyList(),
                workoutSessions = emptyList(),
                sessionSets = emptyList(),
                bodyweightEntries = emptyList(),
                sleepEntries = emptyList(),
                strengthPrExercises = emptyList(),
                strengthPrSelection = emptyList(),
                strengthPrEntries = emptyList(),
            ),
        )

        val error = runCatching { ExportCodec.validate(export) }.exceptionOrNull()
        assertEquals(true, error is IllegalArgumentException)
        assertEquals(true, error?.message?.contains("Unsupported backup version") == true)
    }
}
