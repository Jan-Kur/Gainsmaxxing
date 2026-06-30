package com.gainsmaxxing.ui.workout

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Moon
import com.composables.icons.lucide.Trophy
import com.composables.icons.lucide.X
import com.gainsmaxxing.ui.components.ChartTooltip
import com.gainsmaxxing.ui.components.clickableNoRipple
import com.gainsmaxxing.ui.theme.BgBase
import com.gainsmaxxing.ui.theme.BorderDefault
import com.gainsmaxxing.ui.theme.BorderSubtle
import com.gainsmaxxing.ui.theme.Green500
import com.gainsmaxxing.ui.theme.SleepSleepy
import com.gainsmaxxing.ui.theme.SetPillPrBg
import com.gainsmaxxing.ui.theme.SetPillPrBorder
import com.gainsmaxxing.ui.theme.SetPillPrText
import com.gainsmaxxing.ui.theme.Surface1
import com.gainsmaxxing.ui.theme.Surface3
import com.gainsmaxxing.ui.theme.TextDisabled
import com.gainsmaxxing.ui.theme.TextPrimary
import com.gainsmaxxing.ui.theme.TextSecondary
import com.gainsmaxxing.ui.theme.TextTertiary
import com.gainsmaxxing.ui.theme.caption
import com.gainsmaxxing.ui.theme.exerciseDetails
import com.gainsmaxxing.ui.theme.labelLargeCaps
import com.gainsmaxxing.ui.theme.monoBodyEmphasis
import com.gainsmaxxing.ui.theme.screenTitle
import com.gainsmaxxing.ui.theme.setPill
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gainsmaxxing.domain.SetComparison
import com.gainsmaxxing.domain.WeekMath
import com.gainsmaxxing.domain.WeightFormat
import com.gainsmaxxing.domain.model.WeightUnit

import java.time.LocalDate
import java.time.format.TextStyle as JTextStyle
import java.util.Locale
import kotlin.math.roundToInt

private val LogSetControlRowWidth = 240.dp

private fun weightTickStep(range: Float): Float = SetComparison.weightTickStepKg(range)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var logSheetExerciseId by remember { mutableStateOf<Long?>(null) }
    var logWeightDisplay by remember { mutableFloatStateOf(0f) }
    var logReps by remember { mutableIntStateOf(8) }
    var isWarmup by remember { mutableStateOf(false) }
    var activeHistoryPt by remember { mutableStateOf<Int?>(null) }

    val selectedDay = uiState.splitDays.getOrNull(uiState.selectedDay)
        ?: defaultRestDay(uiState.selectedDay)
    val activeDayIndex = uiState.activeSession?.dayOfWeek ?: uiState.selectedDay
    val activeDay = uiState.splitDays.getOrNull(activeDayIndex) ?: defaultRestDay(activeDayIndex)

    Box(modifier = Modifier.fillMaxSize().background(BgBase)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                uiState.splitDays.ifEmpty { (0..6).map { defaultRestDay(it) } }.forEach { day ->
                    WeekdayButton(
                        dayLabel = WeekMath.dayNames[day.dayOfWeek],
                        typeLabel = day.typeLabel,
                        isSelected = uiState.selectedDay == day.dayOfWeek,
                        onClick = { viewModel.selectDay(day.dayOfWeek) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Text(
                text = selectedDay.workoutName,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (selectedDay.isRestDay) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Icon(Lucide.Moon, null, tint = TextTertiary.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                        Text(
                            "Rest day — recover well.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextTertiary,
                        )
                    }
                } else {
                    selectedDay.exercises.forEach { ex ->
                        ExerciseCard(
                            ex = ex,
                            onInfo = { viewModel.openHistory(ex.id) },
                        )
                    }
                }
            }

            when {
                uiState.activeSession != null && !uiState.showActiveWorkout -> {
                    Box(
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
                    ) {
                        WorkoutCtaButton("Resume Workout", onClick = viewModel::resumeWorkout)
                    }
                }
                !selectedDay.isRestDay && uiState.activeSession == null -> {
                    Box(
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
                    ) {
                        WorkoutCtaButton("Start Workout", onClick = viewModel::startWorkout)
                    }
                }
            }
        }

        if (uiState.showActiveWorkout && uiState.activeSession != null) {
            FullscreenOverlay(onBack = viewModel::minimizeActiveWorkout) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("ACTIVE WORKOUT", style = MaterialTheme.typography.labelMedium, color = TextTertiary)
                        Text(activeDay.workoutName, style = MaterialTheme.typography.screenTitle, color = TextPrimary)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            WorkoutViewModel.formatElapsed(uiState.elapsedMillis),
                            style = MaterialTheme.typography.monoBodyEmphasis,
                            color = Green500,
                        )
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .clickableNoRipple { viewModel.discardWorkout() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Lucide.X, contentDescription = "Discard workout", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    activeDay.exercises.forEach { ex ->
                        val workingSetCount = ex.loggedSets.count { !it.isWarmup }
                        val isDone = workingSetCount >= ex.targetSets
                        ExerciseCard(
                            ex = ex,
                            isDone = isDone,
                            logLabel = if (isDone) "Done" else "+ Set ${workingSetCount + 1}/${ex.targetSets}",
                            onLog = {
                                logSheetExerciseId = ex.id
                                logWeightDisplay = WeightFormat.kgToDisplay(ex.refWeightKg, uiState.weightUnit)
                                logReps = ex.refReps
                                isWarmup = false
                            },
                            onInfo = { viewModel.openHistory(ex.id) },
                        )
                    }
                }

                Box(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
                ) {
                    WorkoutCtaButton("Finish Workout", onClick = viewModel::finishWorkout)
                }
            }
        }

        if (uiState.historyExerciseId != null) {
            val historyPoints = uiState.historySessions
                .sortedBy { it.startedAtEpochMs }
                .mapNotNull { session ->
                session.topWeightKg?.let { top ->
                    session.date to WeightFormat.kgToDisplay(top, uiState.weightUnit)
                }
            }

            FullscreenOverlay(onBack = { viewModel.closeHistory(); activeHistoryPt = null }) {
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
                            .clickableNoRipple { viewModel.closeHistory(); activeHistoryPt = null },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Lucide.ArrowLeft, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(uiState.historyExerciseName, style = MaterialTheme.typography.screenTitle, color = TextPrimary)
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))

                HistoryChart(
                    points = historyPoints,
                    activePt = activeHistoryPt,
                    onPtClick = { activeHistoryPt = if (activeHistoryPt == it) null else it },
                    onDismiss = { activeHistoryPt = null },
                    weightUnit = uiState.weightUnit,
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (uiState.isLoadingHistory) {
                        Text("Loading…", style = MaterialTheme.typography.caption, color = TextTertiary)
                    } else if (uiState.historySessions.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(Lucide.Clock, null, tint = TextTertiary.copy(alpha = 0.4f), modifier = Modifier.size(28.dp))
                            Text("No history yet", style = MaterialTheme.typography.caption, color = TextTertiary)
                        }
                    } else {
                        uiState.historySessions.forEach { session ->
                            HistorySessionCard(session = session)
                        }
                    }
                }
            }
        }
    }

    if (logSheetExerciseId != null) {
        val exerciseId = logSheetExerciseId!!
        val ex = activeDay.exercises.find { it.id == exerciseId }
        val unit = uiState.weightUnit

        ModalBottomSheet(
            onDismissRequest = { logSheetExerciseId = null },
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
                Text(ex?.name ?: "", style = MaterialTheme.typography.titleSmall, color = TextPrimary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))

                val weightLabel = if (ex?.isBodyweight == true) {
                    "Added weight (${WeightFormat.unitLabel(unit)})"
                } else {
                    "Weight (${WeightFormat.unitLabel(unit)})"
                }
                WeightAdjustRow(
                    label = weightLabel,
                    displayValue = logWeightDisplay,
                    weightUnit = unit,
                    onDisplayChange = { logWeightDisplay = it },
                )
                Spacer(Modifier.height(20.dp))

                AdjustRow(
                    label = "Reps",
                    display = "$logReps",
                    onDec = { if (logReps > 1) logReps-- },
                    onInc = { logReps++ },
                )
                Spacer(Modifier.height(28.dp))

                Row(
                    modifier = Modifier
                        .width(LogSetControlRowWidth)
                        .clickableNoRipple { isWarmup = !isWarmup },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Warmup set", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    WarmupToggle(on = isWarmup, onClick = { isWarmup = !isWarmup })
                }
                Spacer(Modifier.height(28.dp))

                WorkoutCtaButton(if (isWarmup) "Log Warmup" else "Log Set") {
                    viewModel.logSet(
                        exerciseId = exerciseId,
                        weightKg = WeightFormat.displayToKg(logWeightDisplay, unit),
                        reps = logReps,
                        isWarmup = isWarmup,
                    )
                    logSheetExerciseId = null
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    "Cancel",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickableNoRipple { logSheetExerciseId = null }
                        .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                )
            }
        }
    }
}

private fun defaultRestDay(dayOfWeek: Int) = SplitDayUi(
    dayOfWeek = dayOfWeek,
    workoutName = "Rest",
    isRestDay = true,
    exercises = emptyList(),
    typeLabel = "",
)

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun SetBadgeRow(
    sets: List<SetUi>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        sets.forEach { s ->
            val label = s.displayLabel
            val pillShape = RoundedCornerShape(20.dp)
            when {
                s.isWarmup -> {
                    Row(
                        modifier = Modifier
                            .clip(pillShape)
                            .background(Surface3)
                            .border(1.dp, TextTertiary.copy(alpha = 0.2f), pillShape)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.setPill,
                            color = TextTertiary,
                        )
                    }
                }
                s.isPr -> {
                    Row(
                        modifier = Modifier
                            .clip(pillShape)
                            .background(SetPillPrBg)
                            .border(1.dp, SetPillPrBorder, pillShape)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.setPill,
                            color = SetPillPrText,
                        )
                        Icon(Lucide.Trophy, null, tint = SetPillPrText, modifier = Modifier.size(11.dp))
                    }
                }
                else -> {
                    Row(
                        modifier = Modifier
                            .clip(pillShape)
                            .background(Green500.copy(alpha = 0.12f))
                            .border(1.dp, Green500.copy(alpha = 0.28f), pillShape)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.setPill,
                            color = Green500,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FullscreenOverlay(
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    BackHandler(onBack = onBack)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            content = content,
        )
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
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelLargeCaps,
            color = TextTertiary,
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.width(LogSetControlRowWidth),
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

private fun formatWeightDisplay(value: Float): String =
    if ((value * 10).roundToInt() % 10 == 0) "${value.toInt()}" else "%.1f".format(value)

@Composable
private fun WeekdayButton(
    dayLabel: String,
    typeLabel: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val borderColor = if (isSelected) Green500.copy(alpha = 0.28f) else BorderDefault
    val dayColor = if (isSelected) Green500 else TextTertiary
    val typeColor = if (isSelected) Green500 else TextDisabled
    val bgColor = if (isSelected) Green500.copy(alpha = 0.12f) else Surface1

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .clickableNoRipple(onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (typeLabel.isEmpty()) {
            Text(
                dayLabel,
                style = MaterialTheme.typography.bodySmall,
                color = dayColor,
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    dayLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = dayColor,
                )
                Text(
                    typeLabel,
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = typeColor,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ExerciseCard(
    ex: ExerciseUi,
    logLabel: String? = null,
    isDone: Boolean = false,
    onLog: (() -> Unit)? = null,
    onInfo: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface1)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                ex.name,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (logLabel != null && onLog != null) {
                    if (isDone) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Surface3)
                                .clickableNoRipple(onLog)
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(Lucide.Check, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                            Text(
                                logLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = TextSecondary,
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SleepSleepy)
                                .clickableNoRipple(onLog)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                logLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = Green500,
                            )
                        }
                    }
                }
                Icon(
                    Lucide.Info,
                    contentDescription = "Exercise history",
                    tint = TextTertiary,
                    modifier = Modifier
                        .size(18.dp)
                        .clickableNoRipple(onInfo),
                )
            }
        }

        Text(
            ex.detailsLine,
            style = MaterialTheme.typography.exerciseDetails,
            color = TextTertiary,
            modifier = Modifier.padding(top = 4.dp),
        )

        if (ex.loggedSets.isNotEmpty()) {
            SetBadgeRow(sets = ex.loggedSets, modifier = Modifier.fillMaxWidth().padding(top = 2.dp))
        }
    }
}

@Composable
private fun HistorySessionCard(session: HistorySessionUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface1)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text = session.dateLabel,
            style = MaterialTheme.typography.caption,
            color = TextTertiary,
        )
        SetBadgeRow(
            sets = session.sets,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
        )
    }
}

@Composable
internal fun WorkoutCtaButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Green500.copy(alpha = 0.12f))
            .border(1.dp, Green500.copy(alpha = 0.28f), RoundedCornerShape(14.dp))
            .clickableNoRipple(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            color = Green500,
        )
    }
}

@Composable
private fun AdjustRow(label: String, display: String, onDec: () -> Unit, onInc: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelLargeCaps,
            color = TextTertiary,
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.width(LogSetControlRowWidth),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AdjBtn("−", onDec)
            Text(
                display,
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                modifier = Modifier.width(80.dp),
                textAlign = TextAlign.Center,
            )
            AdjBtn("+", onInc)
        }
    }
}

@Composable
private fun AdjBtn(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Surface3)
            .clickableNoRipple(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
        )
    }
}

@Composable
private fun WarmupToggle(on: Boolean, onClick: () -> Unit) {
    val trackColor by animateColorAsState(
        targetValue = if (on) Green500.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.12f),
        animationSpec = spring(),
        label = "warmup_toggle_track",
    )
    val knobColor by animateColorAsState(
        targetValue = if (on) Green500 else TextTertiary,
        animationSpec = spring(),
        label = "warmup_toggle_knob_color",
    )
    val knobOffset by animateDpAsState(
        targetValue = if (on) 20.dp else 2.dp,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 600f),
        label = "warmup_toggle_knob_offset",
    )

    Box(
        modifier = Modifier
            .width(44.dp)
            .height(26.dp)
            .clip(CircleShape)
            .background(trackColor)
            .clickableNoRipple(onClick),
    ) {
        Box(
            modifier = Modifier
                .offset(x = knobOffset, y = 2.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(knobColor),
        )
    }
}

@Composable
private fun HistoryChart(
    points: List<Pair<LocalDate, Float>>,
    activePt: Int?,
    onPtClick: (Int) -> Unit,
    onDismiss: () -> Unit,
    weightUnit: WeightUnit,
) {
    if (points.isEmpty()) return

    val chartHeightDp = 110.dp
    val minWeight = points.minOf { it.second }
    val maxWeight = points.maxOf { it.second }
    val span = (maxWeight - minWeight).coerceAtLeast(2.5f)
    val pad = span * 0.14f
    val yMin = maxOf(0f, minWeight - pad)
    val yMax = maxWeight + pad

    val tickStep = weightTickStep(span)
    val tLow = (Math.ceil(yMin / tickStep.toDouble()) * tickStep).toFloat()
    val tHigh = (Math.floor(yMax / tickStep.toDouble()) * tickStep).toFloat()
    val yTicks = if (tLow > tHigh) {
        listOf(Math.round(minWeight / tickStep) * tickStep)
    } else {
        val tMid = (Math.round((tLow + tHigh) / 2f / tickStep) * tickStep).toFloat()
        if (tMid != tLow && tMid != tHigh) listOf(tHigh, tMid, tLow) else listOf(tHigh, tLow)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Surface1)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .pointerInput(Unit) { detectTapGestures { onDismiss() } }
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            "TOP SET WEIGHT",
            style = MaterialTheme.typography.labelLargeCaps,
            color = TextTertiary,
        )
        Spacer(Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.wrapContentWidth().height(chartHeightDp)) {
                yTicks.forEach { tick ->
                    val topFrac = 1f - (tick - yMin) / (yMax - yMin)
                    val density = LocalDensity.current
                    val topPx = with(density) { chartHeightDp.toPx() * topFrac }
                    Text(
                        "${tick.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        modifier = Modifier
                            .offset { IntOffset(0, (topPx - 8.dp.toPx()).roundToInt()) },
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(chartHeightDp)) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(points, yMin, yMax) {
                                val touchSlop = 36.dp.toPx()
                                detectTapGestures { offset ->
                                    val w = size.width.toFloat()
                                    val h = size.height.toFloat()
                                    val nearest = points.indices.minByOrNull { i ->
                                        val x = (i.toFloat() / (points.size - 1).coerceAtLeast(1)) * w
                                        Math.abs(offset.x - x)
                                    }
                                    val onPoint = nearest != null && run {
                                        val x = (nearest.toFloat() / (points.size - 1).coerceAtLeast(1)) * w
                                        val y = h - ((points[nearest].second - yMin) / (yMax - yMin)) * h
                                        Math.hypot((offset.x - x).toDouble(), (offset.y - y).toDouble()) <= touchSlop
                                    }
                                    if (onPoint) onPtClick(nearest!!) else onDismiss()
                                }
                            },
                    ) {
                        val w = size.width
                        val h = size.height
                        fun toX(i: Int) = (i.toFloat() / (points.size - 1).coerceAtLeast(1)) * w
                        fun toY(weight: Float) = h - ((weight - yMin) / (yMax - yMin)) * h
                        val coords = points.indices.map { i -> Offset(toX(i), toY(points[i].second)) }

                        yTicks.forEach { tick ->
                            drawLine(Color(0x0FFFFFFF), Offset(0f, toY(tick)), Offset(w, toY(tick)), 1.dp.toPx())
                        }

                        val linePath = Path()
                        linePath.moveTo(coords[0].x, coords[0].y)
                        for (i in 1 until coords.size) {
                            val prev = coords[i - 1]
                            val cur = coords[i]
                            val cx = (prev.x + cur.x) / 2f
                            linePath.cubicTo(cx, prev.y, cx, cur.y, cur.x, cur.y)
                        }
                        val areaPath = Path().apply {
                            addPath(linePath)
                            lineTo(coords.last().x, h)
                            lineTo(0f, h)
                            close()
                        }
                        drawPath(
                            areaPath,
                            Brush.verticalGradient(
                                listOf(Green500.copy(alpha = 0.16f), Green500.copy(alpha = 0f)),
                                startY = 0f,
                                endY = h,
                            ),
                        )
                        drawPath(linePath, Green500, style = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

                        coords.forEachIndexed { i, p ->
                            val isActive = activePt == i
                            if (isActive) {
                                drawCircle(Green500, 7.dp.toPx(), p)
                                drawCircle(Color.White, 4.dp.toPx(), p)
                            } else {
                                drawCircle(Green500, 1.8.dp.toPx(), p)
                            }
                        }
                    }

                    if (activePt != null && activePt in points.indices) {
                        val (date, weight) = points[activePt]
                        val xFrac = activePt.toFloat() / (points.size - 1).coerceAtLeast(1)
                        val yFrac = 1f - (weight - yMin) / (yMax - yMin)
                        val dotXDp = maxWidth * xFrac
                        val dotYDp = maxHeight * yFrac
                        val tooltipDate = "${date.dayOfMonth} ${date.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH)}"
                        val tooltipValue = "${if ((weight * 10).roundToInt() % 10 == 0) weight.toInt() else "%.1f".format(weight)} ${WeightFormat.unitLabel(weightUnit)}"
                        ChartTooltip(
                            chartWidth = maxWidth,
                            chartHeight = maxHeight,
                            anchorX = dotXDp,
                            anchorTop = dotYDp,
                            topOverflow = 56.dp,
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Surface3)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Column {
                                    Text(tooltipDate, style = MaterialTheme.typography.caption, color = TextTertiary)
                                    Text(tooltipValue, style = MaterialTheme.typography.monoBodyEmphasis, color = TextPrimary)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
                BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(16.dp)) {
                    val totalWidth = maxWidth
                    val monthGroups = LinkedHashMap<String, IntRange>()
                    points.forEachIndexed { i, (date, _) ->
                        val key = "${date.year}-${date.monthValue}"
                        val existing = monthGroups[key]
                        monthGroups[key] = if (existing == null) i..i else existing.first..i
                    }
                    monthGroups.forEach { (_, range) ->
                        val centerFrac = (range.first + range.last).toFloat() / 2f / (points.size - 1).coerceAtLeast(1)
                        val label = points[range.first].first.month.getDisplayName(JTextStyle.SHORT, Locale.ENGLISH)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = totalWidth * centerFrac)
                                .width(0.dp)
                                .wrapContentWidth(unbounded = true),
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }
                }
            }
        }
    }
}
