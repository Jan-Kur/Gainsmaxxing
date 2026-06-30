package com.gainsmaxxing.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gainsmaxxing.data.repository.CalendarRepository
import com.gainsmaxxing.domain.model.CalendarActivityType
import com.gainsmaxxing.domain.model.TimeSlot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CalendarEditUiState(
    val days: List<CalendarDayDisplay> = emptyList(),
    val typeOrder: List<Long> = emptyList(),
    val template: Map<Pair<Int, TimeSlot>, Long?> = emptyMap(),
    val activityTypes: Map<Long, CalendarActivityType> = emptyMap(),
)

@HiltViewModel
class CalendarEditViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
) : ViewModel() {

    val uiState = combine(
        calendarRepository.observeActivityTypes(),
        calendarRepository.observeTemplate(),
    ) { types, template ->
        val typeById = types.associateBy { it.id }
        val days = (0..6).map { dayIndex ->
            CalendarDayDisplay(
                dayIndex = dayIndex,
                morning = CalendarSlotDisplay(
                    activityType = template[dayIndex to TimeSlot.MORNING]?.let { typeById[it] },
                ),
                evening = CalendarSlotDisplay(
                    activityType = template[dayIndex to TimeSlot.EVENING]?.let { typeById[it] },
                ),
            )
        }
        CalendarEditUiState(
            days = days,
            typeOrder = types.map { it.id },
            template = template,
            activityTypes = typeById,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarEditUiState(),
    )

    fun cycleSlot(dayIndex: Int, slot: TimeSlot) {
        val state = uiState.value
        val currentId = state.template[dayIndex to slot]
        val nextId = com.gainsmaxxing.domain.CalendarCycle.nextTypeId(currentId, state.typeOrder)
        viewModelScope.launch {
            calendarRepository.setTemplateSlot(dayIndex, slot, nextId)
        }
    }
}
