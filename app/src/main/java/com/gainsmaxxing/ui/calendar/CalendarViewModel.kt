package com.gainsmaxxing.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gainsmaxxing.data.repository.CalendarRepository
import com.gainsmaxxing.data.repository.WeekSchedule
import com.gainsmaxxing.domain.model.CalendarActivityType
import com.gainsmaxxing.domain.model.TimeSlot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CalendarUiState(
    val weekOffset: Int = 0,
    val weekLabel: String = "",
    val days: List<CalendarDayDisplay> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
) : ViewModel() {

    private val _weekOffset = MutableStateFlow(0)
    val weekOffset: StateFlow<Int> = _weekOffset.asStateFlow()

    private val today = LocalDate.now()

    val uiState: StateFlow<CalendarUiState> = _weekOffset
        .map { offset ->
            val weekStart = mondayOfWeekContaining(today).plusWeeks(offset.toLong())
            weekStart to offset
        }
        .flatMapLatest { (weekStart, offset) ->
            calendarRepository.observeWeekSchedule(weekStart).map { schedule ->
                buildUiState(weekStart, offset, schedule)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CalendarUiState(),
        )

    fun previousWeek() {
        _weekOffset.value -= 1
    }

    fun nextWeek() {
        _weekOffset.value += 1
    }

    fun toggleSkip(date: LocalDate, slot: TimeSlot) {
        viewModelScope.launch {
            calendarRepository.toggleSkip(date, slot)
        }
    }

    private fun buildUiState(
        weekStart: LocalDate,
        weekOffset: Int,
        schedule: WeekSchedule,
    ): CalendarUiState {
        val days = (0..6).map { dayIndex ->
            val date = weekStart.plusDays(dayIndex.toLong())
            val morningTypeId = schedule.template[dayIndex to TimeSlot.MORNING]
            val eveningTypeId = schedule.template[dayIndex to TimeSlot.EVENING]
            CalendarDayDisplay(
                dayIndex = dayIndex,
                date = date,
                isToday = date == today,
                morning = CalendarSlotDisplay(
                    activityType = morningTypeId?.let { schedule.activityTypes[it] },
                    isSkipped = (date to TimeSlot.MORNING) in schedule.skips,
                    onTap = morningTypeId?.let {
                        { toggleSkip(date, TimeSlot.MORNING) }
                    },
                ),
                evening = CalendarSlotDisplay(
                    activityType = eveningTypeId?.let { schedule.activityTypes[it] },
                    isSkipped = (date to TimeSlot.EVENING) in schedule.skips,
                    onTap = eveningTypeId?.let {
                        { toggleSkip(date, TimeSlot.EVENING) }
                    },
                ),
            )
        }
        return CalendarUiState(
            weekOffset = weekOffset,
            weekLabel = formatWeekRange(weekStart),
            days = days,
        )
    }
}
