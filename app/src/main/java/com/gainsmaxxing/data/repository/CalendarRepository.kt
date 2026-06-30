package com.gainsmaxxing.data.repository

import com.gainsmaxxing.data.db.dao.CalendarDao
import com.gainsmaxxing.data.db.entities.ActivityTypeEntity
import com.gainsmaxxing.data.db.entities.CalendarSkipEntity
import com.gainsmaxxing.data.db.entities.CalendarTemplateSlotEntity
import com.gainsmaxxing.data.mapper.toDomain
import com.gainsmaxxing.data.mapper.toDomainKey
import com.gainsmaxxing.data.mapper.toEntity
import com.gainsmaxxing.data.mapper.toStorageKey
import com.gainsmaxxing.domain.model.CalendarActivityType
import com.gainsmaxxing.domain.model.TimeSlot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

typealias TemplateSlotKey = Pair<Int, TimeSlot>
typealias SkipSlotKey = Pair<LocalDate, TimeSlot>

@Singleton
class CalendarRepository @Inject constructor(
    private val calendarDao: CalendarDao,
) {
    fun observeActivityTypes(): Flow<List<CalendarActivityType>> =
        calendarDao.observeActivityTypes().map { rows ->
            rows.map { it.toDomain() }
        }

    fun observeTemplate(): Flow<Map<TemplateSlotKey, Long?>> =
        calendarDao.observeTemplateSlots().map { rows ->
            rows.associate { entity ->
                val (day, slot) = entity.toDomainKey()
                (day to slot) to entity.activityTypeId
            }
        }

    fun observeSkipsForWeek(weekStart: LocalDate): Flow<Set<SkipSlotKey>> {
        val weekEnd = weekStart.plusDays(6)
        return calendarDao.observeSkipsInRange(
            weekStart.toString(),
            weekEnd.toString(),
        ).map { rows ->
            rows.map { it.toDomainKey() }.toSet()
        }
    }

    fun observeWeekSchedule(weekStart: LocalDate): Flow<WeekSchedule> =
        combine(
            observeActivityTypes(),
            observeTemplate(),
            observeSkipsForWeek(weekStart),
        ) { types, template, skips ->
            WeekSchedule(
                activityTypes = types.associateBy { it.id },
                typeOrder = types.map { it.id },
                template = template,
                skips = skips,
            )
        }

    suspend fun addActivityType(
        name: String,
        colorPaletteIndex: Int,
        iconKey: String,
        customColorArgb: Int? = null,
    ): Long {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Name cannot be blank" }
        require(colorPaletteIndex in 0 until ACTIVITY_COLOR_COUNT) { "Invalid color" }
        return calendarDao.insertActivityType(
            ActivityTypeEntity(
                name = trimmed,
                colorPaletteIndex = colorPaletteIndex,
                customColorArgb = customColorArgb,
                iconKey = iconKey,
                sortOrder = calendarDao.maxActivityTypeSortOrder() + 1,
            ),
        )
    }

    suspend fun updateActivityType(type: CalendarActivityType) {
        val trimmed = type.name.trim()
        require(trimmed.isNotEmpty()) { "Name cannot be blank" }
        require(type.colorPaletteIndex in 0 until ACTIVITY_COLOR_COUNT) { "Invalid color" }
        calendarDao.upsertActivityType(
            type.copy(name = trimmed).toEntity(),
        )
    }

    suspend fun deleteActivityType(id: Long) {
        calendarDao.deleteActivityTypeAndClearSlots(id)
    }

    suspend fun saveActivityTypeOrder(orderedIds: List<Long>) {
        val existing = calendarDao.getActivityTypes().associateBy { it.id }
        val entities = orderedIds
            .filter { it in existing }
            .mapIndexed { index, id ->
                existing.getValue(id).copy(sortOrder = index)
            }
        if (entities.isNotEmpty()) {
            calendarDao.upsertActivityTypes(entities)
        }
    }

    suspend fun setTemplateSlot(dayOfWeek: Int, slot: TimeSlot, activityTypeId: Long?) {
        require(dayOfWeek in 0..6) { "Invalid day" }
        calendarDao.upsertTemplateSlot(
            CalendarTemplateSlotEntity(
                dayOfWeek = dayOfWeek,
                slot = slot.toStorageKey(),
                activityTypeId = activityTypeId,
            ),
        )
    }

    suspend fun toggleSkip(date: LocalDate, slot: TimeSlot) {
        val dateKey = date.toString()
        val slotKey = slot.toStorageKey()
        if (calendarDao.isSkipped(dateKey, slotKey)) {
            calendarDao.deleteSkip(dateKey, slotKey)
        } else {
            calendarDao.insertSkip(CalendarSkipEntity(date = dateKey, slot = slotKey))
        }
    }

    companion object {
        const val ACTIVITY_COLOR_COUNT = 12
    }
}

data class WeekSchedule(
    val activityTypes: Map<Long, CalendarActivityType>,
    val typeOrder: List<Long>,
    val template: Map<TemplateSlotKey, Long?>,
    val skips: Set<SkipSlotKey>,
)
