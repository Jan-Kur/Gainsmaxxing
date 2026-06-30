package com.gainsmaxxing.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.ArrowDown
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.ArrowUp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Trash2
import com.gainsmaxxing.domain.WeekMath
import com.gainsmaxxing.ui.components.clickableNoRipple
import com.gainsmaxxing.ui.theme.BgBase
import com.gainsmaxxing.ui.theme.BorderSubtle
import com.gainsmaxxing.ui.theme.Green500
import com.gainsmaxxing.ui.theme.Surface1
import com.gainsmaxxing.ui.theme.Surface2
import com.gainsmaxxing.ui.theme.TextPrimary
import com.gainsmaxxing.ui.theme.TextSecondary
import com.gainsmaxxing.ui.theme.TextTertiary
import com.gainsmaxxing.ui.theme.caption
import com.gainsmaxxing.ui.theme.screenTitle

@Composable
fun SplitEditorScreen(
    onClose: () -> Unit,
    viewModel: SplitEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedDay = state.days.getOrNull(state.selectedDay)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .clickableNoRipple(onClose),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Lucide.ArrowLeft, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Edit Workout Split",
                style = MaterialTheme.typography.screenTitle,
                color = TextPrimary,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            WeekMath.dayNames.forEachIndexed { index, label ->
                val selected = state.selectedDay == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) Green500.copy(alpha = 0.12f) else Surface1)
                        .border(
                            1.dp,
                            if (selected) Green500.copy(alpha = 0.28f) else BorderSubtle,
                            RoundedCornerShape(10.dp),
                        )
                        .clickableNoRipple { viewModel.selectDay(index) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(label, style = MaterialTheme.typography.labelMedium, color = if (selected) Green500 else TextTertiary)
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (selectedDay != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Rest day", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    RestDayToggle(
                        on = selectedDay.isRestDay,
                        onClick = { viewModel.setRestDay(!selectedDay.isRestDay) },
                    )
                }

                if (!selectedDay.isRestDay) {
                    EditorField(
                        label = "Workout name",
                        value = selectedDay.workoutName,
                        onValueChange = viewModel::setWorkoutName,
                    )

                    selectedDay.exercises.forEach { exercise ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Surface1)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            EditorField(
                                label = "Exercise",
                                value = exercise.name,
                                onValueChange = { value ->
                                    viewModel.updateExercise(exercise.localId) { it.copy(name = value) }
                                },
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                CounterField(
                                    label = "Sets",
                                    value = exercise.targetSets,
                                    onChange = { value ->
                                        viewModel.updateExercise(exercise.localId) { it.copy(targetSets = value) }
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                                CounterField(
                                    label = "Reps",
                                    value = exercise.targetReps,
                                    onChange = { value ->
                                        viewModel.updateExercise(exercise.localId) { it.copy(targetReps = value) }
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Bodyweight + added weight", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                                RestDayToggle(
                                    on = exercise.isBodyweight,
                                    onClick = {
                                        viewModel.updateExercise(exercise.localId) {
                                            it.copy(isBodyweight = !it.isBodyweight)
                                        }
                                    },
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButtonSmall(Lucide.ArrowUp) { viewModel.moveExercise(exercise.localId, -1) }
                                    IconButtonSmall(Lucide.ArrowDown) { viewModel.moveExercise(exercise.localId, 1) }
                                }
                                IconButtonSmall(Lucide.Trash2) { viewModel.removeExercise(exercise.localId) }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Surface2)
                            .clickableNoRipple { viewModel.addExercise() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Lucide.Plus, null, tint = Green500, modifier = Modifier.size(16.dp))
                            Text("Add exercise", color = Green500, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            state.saveMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.caption,
                    color = TextTertiary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Center,
                )
            }
            WorkoutCtaButton(
                label = if (state.isSaving) "Saving…" else "Save ${WeekMath.dayNames[state.selectedDay]}",
                onClick = { viewModel.saveSelectedDay() },
            )
        }
    }
}

@Composable
private fun EditorField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = TextTertiary)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
            cursorBrush = SolidColor(Green500),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Surface1)
                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            singleLine = true,
        )
    }
}

@Composable
private fun CounterField(
    label: String,
    value: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = TextTertiary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Surface1)
                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("−", modifier = Modifier.clickableNoRipple { if (value > 1) onChange(value - 1) }, color = TextSecondary)
            Text("$value", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
            Text("+", modifier = Modifier.clickableNoRipple { onChange(value + 1) }, color = TextSecondary)
        }
    }
}

@Composable
private fun IconButtonSmall(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .clickableNoRipple(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun RestDayToggle(on: Boolean, onClick: () -> Unit) {
    val trackColor = if (on) Green500.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.12f)
    val knobColor = if (on) Green500 else TextTertiary
    Box(
        modifier = Modifier
            .width(44.dp)
            .height(26.dp)
            .clip(CircleShape)
            .background(trackColor)
            .clickableNoRipple(onClick),
        contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .padding(2.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(knobColor),
        )
    }
}
