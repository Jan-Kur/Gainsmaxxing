package com.gainsmaxxing.data.mapper

import com.gainsmaxxing.data.db.entities.BodyweightEntryEntity
import com.gainsmaxxing.data.db.entities.SleepEntryEntity
import com.gainsmaxxing.data.db.entities.StrengthPrEntryEntity
import com.gainsmaxxing.domain.model.BodyweightEntry
import com.gainsmaxxing.domain.model.EnergyTag
import com.gainsmaxxing.domain.model.SleepEntry
import com.gainsmaxxing.domain.model.StrengthPrEntry
import java.time.Instant
import java.time.LocalDate

fun BodyweightEntryEntity.toDomain(): BodyweightEntry = BodyweightEntry(
    date = LocalDate.parse(date),
    weightKg = weightKg,
)

fun BodyweightEntry.toEntity(): BodyweightEntryEntity = BodyweightEntryEntity(
    date = date.toString(),
    weightKg = weightKg,
)

fun SleepEntryEntity.toDomain(): SleepEntry = SleepEntry(
    date = LocalDate.parse(date),
    hours = hours,
    energyTag = EnergyTag.valueOf(energyTag),
)

fun SleepEntry.toEntity(): SleepEntryEntity = SleepEntryEntity(
    date = date.toString(),
    hours = hours,
    energyTag = energyTag.name,
)

fun StrengthPrEntryEntity.toDomain(): StrengthPrEntry = StrengthPrEntry(
    id = id,
    exerciseName = exerciseName,
    oneRmKg = oneRmKg,
    loggedAt = Instant.ofEpochMilli(loggedAtEpochMs),
)

fun StrengthPrEntry.toEntity(): StrengthPrEntryEntity = StrengthPrEntryEntity(
    id = id,
    exerciseName = exerciseName,
    oneRmKg = oneRmKg,
    loggedAtEpochMs = loggedAt.toEpochMilli(),
)
