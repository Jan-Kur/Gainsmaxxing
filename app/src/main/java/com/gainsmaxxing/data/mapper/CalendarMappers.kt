package com.gainsmaxxing.data.mapper

import com.gainsmaxxing.data.db.entities.ActivityTypeEntity
import com.gainsmaxxing.data.db.entities.CalendarSkipEntity
import com.gainsmaxxing.data.db.entities.CalendarTemplateSlotEntity
import com.gainsmaxxing.domain.model.CalendarActivityType
import com.gainsmaxxing.domain.model.TimeSlot
import java.time.LocalDate

fun ActivityTypeEntity.toDomain(): CalendarActivityType = CalendarActivityType(
    id = id,
    name = name,
    colorPaletteIndex = colorPaletteIndex,
    customColorArgb = customColorArgb,
    iconKey = iconKey,
    sortOrder = sortOrder,
)

fun CalendarActivityType.toEntity(): ActivityTypeEntity = ActivityTypeEntity(
    id = id,
    name = name,
    colorPaletteIndex = colorPaletteIndex,
    customColorArgb = customColorArgb,
    iconKey = iconKey,
    sortOrder = sortOrder,
)

fun TimeSlot.toStorageKey(): String = name

fun String.toTimeSlot(): TimeSlot = TimeSlot.valueOf(this)

fun templateKey(dayOfWeek: Int, slot: TimeSlot): Pair<Int, TimeSlot> = dayOfWeek to slot

fun CalendarTemplateSlotEntity.toDomainKey(): Pair<Int, TimeSlot> =
    dayOfWeek to slot.toTimeSlot()

fun skipKey(date: LocalDate, slot: TimeSlot): String = "${date}_${slot.name}"

fun CalendarSkipEntity.toDomainKey(): Pair<LocalDate, TimeSlot> =
    LocalDate.parse(date) to slot.toTimeSlot()
