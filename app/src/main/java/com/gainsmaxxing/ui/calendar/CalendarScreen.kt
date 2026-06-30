package com.gainsmaxxing.ui.calendar

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gainsmaxxing.domain.model.TimeSlot
import com.gainsmaxxing.ui.theme.BgBase
import com.gainsmaxxing.ui.theme.TextTertiary

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        CalendarWeekHeader(
            weekLabel = uiState.weekLabel,
            onPreviousWeek = viewModel::previousWeek,
            onNextWeek = viewModel::nextWeek,
        )

        CalendarSlotColumnsHeader()
        Spacer(Modifier.height(8.dp))

        CalendarWeekGrid(
            days = uiState.days,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 8.dp),
        )
    }
}

@Composable
fun CalendarEditScreen(
    onClose: () -> Unit,
    viewModel: CalendarEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(onBack = onClose)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        CalendarSettingsHeader(title = "Edit Calendar", onClose = onClose)

        Text(
            text = "Tap any slot to cycle through your activity types and empty.",
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        CalendarSlotColumnsHeader()
        Spacer(Modifier.height(8.dp))

        CalendarWeekGrid(
            days = uiState.days.map { day ->
                day.copy(
                    morning = day.morning.copy(
                        onTap = { viewModel.cycleSlot(day.dayIndex, TimeSlot.MORNING) },
                    ),
                    evening = day.evening.copy(
                        onTap = { viewModel.cycleSlot(day.dayIndex, TimeSlot.EVENING) },
                    ),
                )
            },
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 8.dp),
        )
    }
}
