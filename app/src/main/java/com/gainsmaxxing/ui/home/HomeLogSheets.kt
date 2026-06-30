package com.gainsmaxxing.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gainsmaxxing.domain.SleepFormat
import com.gainsmaxxing.domain.WeightFormat
import com.gainsmaxxing.domain.model.EnergyTag
import com.gainsmaxxing.domain.model.WeightUnit
import com.gainsmaxxing.ui.components.clickableNoRipple
import com.gainsmaxxing.ui.theme.BgBase
import com.gainsmaxxing.ui.theme.Green500
import com.gainsmaxxing.ui.theme.Surface2
import com.gainsmaxxing.ui.theme.Surface4
import com.gainsmaxxing.ui.theme.TextPrimary
import com.gainsmaxxing.ui.theme.TextSecondary
import com.gainsmaxxing.ui.theme.TextTertiary
import com.gainsmaxxing.ui.theme.labelLargeCaps
import com.gainsmaxxing.ui.workout.WorkoutCtaButton
import kotlin.math.roundToInt

private val LogControlRowWidth = 240.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyweightLogSheet(
    weightUnit: WeightUnit,
    initialDisplayKg: Float,
    onDismiss: () -> Unit,
    onSave: (weightKg: Float) -> Unit,
) {
    var displayValue by remember {
        mutableFloatStateOf(WeightFormat.kgToDisplay(initialDisplayKg, weightUnit))
    }

    WeightLogSheet(
        title = "Log bodyweight",
        weightLabel = "Weight (${WeightFormat.unitLabel(weightUnit)})",
        weightUnit = weightUnit,
        displayValue = displayValue,
        onDisplayChange = { displayValue = it },
        onDismiss = onDismiss,
        onSave = {
            val kg = WeightFormat.displayToKg(displayValue, weightUnit)
            if (kg > 0f) onSave(kg)
        },
        saveLabel = "Save weigh-in",
    )
}

@Composable
fun StrengthPrLogSheet(
    exerciseName: String,
    weightUnit: WeightUnit,
    initialDisplayKg: Float,
    onDismiss: () -> Unit,
    onSave: (oneRmKg: Float) -> Unit,
) {
    var displayValue by remember {
        mutableFloatStateOf(WeightFormat.kgToDisplay(initialDisplayKg, weightUnit))
    }

    WeightLogSheet(
        title = exerciseName,
        weightLabel = "1RM (${WeightFormat.unitLabel(weightUnit)})",
        weightUnit = weightUnit,
        displayValue = displayValue,
        onDisplayChange = { displayValue = it },
        onDismiss = onDismiss,
        onSave = {
            val kg = WeightFormat.displayToKg(displayValue, weightUnit)
            if (kg > 0f) onSave(kg)
        },
        saveLabel = "Save 1RM",
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeightLogSheet(
    title: String,
    weightLabel: String,
    weightUnit: WeightUnit,
    displayValue: Float,
    onDisplayChange: (Float) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    saveLabel: String,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = BgBase,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 6.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            WeightAdjustRow(
                label = weightLabel,
                displayValue = displayValue,
                weightUnit = weightUnit,
                onDisplayChange = onDisplayChange,
            )
            Spacer(Modifier.height(28.dp))
            WorkoutCtaButton(saveLabel, onClick = onSave)
            Spacer(Modifier.height(4.dp))
            Text(
                "Cancel",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickableNoRipple(onDismiss)
                    .padding(vertical = 10.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepLogSheet(
    onDismiss: () -> Unit,
    onSave: (hours: Float, energyTag: EnergyTag) -> Unit,
) {
    var sleepHours by remember { mutableIntStateOf(7) }
    var sleepMinutes by remember { mutableIntStateOf(0) }
    var energyTag by remember { mutableStateOf(EnergyTag.NEUTRAL) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = BgBase,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 6.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Log sleep", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Spacer(Modifier.height(24.dp))

            IntAdjustRow(
                label = "Hours",
                value = sleepHours,
                min = 0,
                max = 14,
                onValueChange = { sleepHours = it },
            )
            Spacer(Modifier.height(20.dp))
            IntAdjustRow(
                label = "Minutes",
                value = sleepMinutes,
                min = 0,
                max = 59,
                onValueChange = { sleepMinutes = it },
            )

            Spacer(Modifier.height(24.dp))
            Text("ENERGY", style = MaterialTheme.typography.labelLargeCaps, color = TextTertiary)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                EnergyTag.entries.forEach { tag ->
                    val selected = energyTag == tag
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) Surface4 else Surface2)
                            .clickableNoRipple { energyTag = tag }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            tag.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selected) TextPrimary else TextSecondary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            WorkoutCtaButton("Save sleep") {
                val hours = SleepFormat.hoursAndMinutesToFloat(sleepHours, sleepMinutes)
                if (hours >= 1f / 60f) onSave(hours, energyTag)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Cancel",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickableNoRipple(onDismiss)
                    .padding(vertical = 10.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary,
            )
        }
    }
}

@Composable
private fun WeightAdjustRow(
    label: String,
    displayValue: Float,
    weightUnit: WeightUnit,
    onDisplayChange: (Float) -> Unit,
) {
    var editing by remember { mutableStateOf(false) }
    var draft by remember(displayValue, editing) {
        mutableStateOf(formatWeightDisplay(displayValue))
    }
    val stepDisplay = WeightFormat.kgToDisplay(WeightFormat.STEP_KG, weightUnit)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelLargeCaps, color = TextTertiary)
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.width(LogControlRowWidth),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AdjBtn("−") {
                editing = false
                onDisplayChange(maxOf(0f, displayValue - stepDisplay))
            }
            if (editing) {
                BasicTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                    ),
                    cursorBrush = SolidColor(Green500),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.width(80.dp),
                )
            } else {
                Text(
                    formatWeightDisplay(displayValue),
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    modifier = Modifier
                        .width(80.dp)
                        .clickableNoRipple {
                            draft = formatWeightDisplay(displayValue)
                            editing = true
                        },
                    textAlign = TextAlign.Center,
                )
            }
            AdjBtn("+") {
                editing = false
                onDisplayChange(displayValue + stepDisplay)
            }
        }
        if (editing) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Done",
                modifier = Modifier.clickableNoRipple {
                    val parsed = draft.replace(',', '.').toFloatOrNull() ?: displayValue
                    onDisplayChange(maxOf(0f, parsed))
                    editing = false
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Green500,
            )
        }
    }
}

@Composable
private fun IntAdjustRow(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    onValueChange: (Int) -> Unit,
) {
    var editing by remember { mutableStateOf(false) }
    var draft by remember(value, editing) { mutableStateOf(value.toString()) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelLargeCaps, color = TextTertiary)
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.width(LogControlRowWidth),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AdjBtn("−") {
                editing = false
                onValueChange((value - 1).coerceIn(min, max))
            }
            if (editing) {
                BasicTextField(
                    value = draft,
                    onValueChange = { draft = it.filter { ch -> ch.isDigit() } },
                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                    ),
                    cursorBrush = SolidColor(Green500),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.width(80.dp),
                )
            } else {
                Text(
                    "$value",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    modifier = Modifier
                        .width(80.dp)
                        .clickableNoRipple {
                            draft = value.toString()
                            editing = true
                        },
                    textAlign = TextAlign.Center,
                )
            }
            AdjBtn("+") {
                editing = false
                onValueChange((value + 1).coerceIn(min, max))
            }
        }
        if (editing) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Done",
                modifier = Modifier.clickableNoRipple {
                    val parsed = draft.toIntOrNull()?.coerceIn(min, max) ?: value
                    onValueChange(parsed)
                    editing = false
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Green500,
            )
        }
    }
}

@Composable
private fun AdjBtn(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Surface2)
            .clickableNoRipple(onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, color = TextSecondary)
    }
}

private fun formatWeightDisplay(value: Float): String =
    if ((value * 10).roundToInt() % 10 == 0) "${value.toInt()}" else "%.1f".format(value)
