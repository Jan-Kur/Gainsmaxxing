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
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalConfiguration
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
import java.time.LocalDate
import java.time.format.TextStyle as JTextStyle
import java.util.Locale
import kotlin.math.roundToInt

data class ExerciseDef(val id: String, val name: String, val sets: Int, val reps: Int, val refWeight: Float, val unit: String)
data class SetEntry(val weight: Float, val reps: Int, val isWarmup: Boolean, val isPR: Boolean = false)

private val split = mapOf(
    1 to Pair("Upper A", listOf(
        ExerciseDef("bench", "Bench Press", 4, 8, 100f, "kg"),
        ExerciseDef("ohp", "Overhead Press", 3, 10, 70f, "kg"),
        ExerciseDef("pullup", "Pull-ups", 3, 8, 15f, "BW"),
        ExerciseDef("dbrow", "Dumbbell Row", 3, 12, 32f, "kg"),
        ExerciseDef("lateral", "Lateral Raises", 3, 15, 12f, "kg"),
    )),
    3 to Pair("Lower A", listOf(
        ExerciseDef("squat", "Back Squat", 4, 5, 137.5f, "kg"),
        ExerciseDef("rdl", "Romanian DL", 3, 8, 90f, "kg"),
        ExerciseDef("legpress", "Leg Press", 3, 12, 180f, "kg"),
        ExerciseDef("legcurl", "Leg Curl", 3, 12, 50f, "kg"),
        ExerciseDef("calf", "Calf Raises", 4, 15, 70f, "kg"),
    )),
    4 to Pair("Upper B", listOf(
        ExerciseDef("dl", "Deadlift", 3, 5, 177.5f, "kg"),
        ExerciseDef("incline", "Incline Press", 3, 10, 36f, "kg"),
        ExerciseDef("cable", "Cable Rows", 3, 12, 65f, "kg"),
        ExerciseDef("dips", "Weighted Dips", 3, 10, 20f, "BW"),
        ExerciseDef("curl", "Bicep Curls", 3, 12, 22f, "kg"),
    )),
    6 to Pair("Lower B", listOf(
        ExerciseDef("frontsq", "Front Squat", 3, 6, 100f, "kg"),
        ExerciseDef("hipth", "Hip Thrust", 3, 10, 120f, "kg"),
        ExerciseDef("lunge", "Walking Lunges", 3, 12, 30f, "kg"),
        ExerciseDef("legext", "Leg Extension", 3, 15, 60f, "kg"),
        ExerciseDef("scalf", "Seated Calves", 4, 20, 40f, "kg"),
    )),
)

private val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

private fun todayDayIndex(): Int {
    val dow = LocalDate.now().dayOfWeek.value // 1=Mon..7=Sun
    return dow - 1 // 0=Mon..6=Sun
}

/**
     * Formats a weight for display.
     *
     * @param w The weight to format.
     * @return The weight as an integer when the tenths digit is zero, or with one decimal place otherwise.
     */
    private fun formatWeight(w: Float): String =
    if ((w * 10).roundToInt() % 10 == 0) "${w.toInt()}" else "%.1f".format(w)

private val LogSetControlRowWidth = 240.dp

/**
 * Builds the detail line for an exercise.
 *
 * @param ex The exercise to describe.
 * @return A string containing the set count, rep count, and reference weight.
 */
private fun exerciseDetailsLine(ex: ExerciseDef): String {
    val weightPart = if (ex.unit == "BW") "+${ex.refWeight.toInt()} kg BW" else "${formatWeight(ex.refWeight)} kg"
    return "${ex.sets} sets · ${ex.reps} reps · $weightPart"
}

/**
 * Displays the workout planner, active workout sheet, history view, and set logging controls.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkoutScreen() {
    var selectedDay by rememberSaveable { mutableIntStateOf(todayDayIndex()) }
    var showActiveWorkout by rememberSaveable { mutableStateOf(false) }
    var activeSets by remember { mutableStateOf(mapOf<String, List<SetEntry>>()) }
    var logSheetExId by remember { mutableStateOf<String?>(null) }
    var logWeight by remember { mutableFloatStateOf(80f) }
    var logReps by remember { mutableIntStateOf(8) }
    var isWarmup by remember { mutableStateOf(false) }
    var historyExId by remember { mutableStateOf<String?>(null) }
    var activeHistoryPt by remember { mutableStateOf<Int?>(null) }

    val currentSplit = split[selectedDay]
    val isRestDay = currentSplit == null
    val workoutName = currentSplit?.first ?: "Rest"
    val exercises = currentSplit?.second ?: emptyList()

    Box(modifier = Modifier.fillMaxSize().background(BgBase)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            // Day pill row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                (0..6).forEach { dayIdx ->
                    val dayData = split[dayIdx]
                    val typeLabel = when {
                        dayData == null -> ""
                        else -> (dayData.first.split(" ").firstOrNull() ?: dayData.first).uppercase()
                    }
                    WeekdayButton(
                        dayLabel = dayNames[dayIdx],
                        typeLabel = typeLabel,
                        isSelected = selectedDay == dayIdx,
                        onClick = { selectedDay = dayIdx },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Workout name
            Text(
                text = workoutName,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )

            // Exercise list
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (isRestDay) {
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
                    exercises.forEach { ex ->
                        ExerciseCard(
                            ex = ex,
                            sets = emptyList(),
                            onInfo = { historyExId = ex.id },
                        )
                    }
                }
            }

            // Start Workout button
            if (!isRestDay) {
                Box(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
                ) {
                    WorkoutCtaButton("Start Workout") { showActiveWorkout = true }
                }
            }
        }

        if (showActiveWorkout) {
            FullscreenWorkoutSheet(onDismissRequest = { showActiveWorkout = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    Text(
                        "ACTIVE WORKOUT",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextTertiary,
                    )
                    Text(
                        workoutName,
                        style = MaterialTheme.typography.screenTitle,
                        color = TextPrimary,
                    )
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    exercises.forEach { ex ->
                        val sets = activeSets[ex.id] ?: emptyList()
                        val workingSetCount = sets.count { !it.isWarmup }
                        val isDone = workingSetCount >= ex.sets
                        ExerciseCard(
                            ex = ex,
                            sets = sets,
                            isDone = isDone,
                            logLabel = if (isDone) "Done" else "+ Set ${workingSetCount + 1}/${ex.sets}",
                            onLog = {
                                logSheetExId = ex.id
                                logWeight = ex.refWeight
                                logReps = ex.reps
                                isWarmup = false
                            },
                            onInfo = { historyExId = ex.id },
                        )
                    }
                }

                Box(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
                ) {
                    WorkoutCtaButton("Finish Workout") { showActiveWorkout = false }
                }
            }
        }

        if (historyExId != null) {
            val exId = historyExId
            val exName = exercises.find { it.id == exId }?.name ?: (split.values.flatMap { it.second }.find { it.id == exId }?.name ?: "")

            FullscreenWorkoutSheet(onDismissRequest = { historyExId = null; activeHistoryPt = null }) {
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
                            .clickableNoRipple { historyExId = null; activeHistoryPt = null },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Lucide.ArrowLeft, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        exName,
                        style = MaterialTheme.typography.screenTitle,
                        color = TextPrimary,
                    )
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))

                // 1RM chart
                HistoryChart(
                    activePt = activeHistoryPt,
                    onPtClick = { activeHistoryPt = if (activeHistoryPt == it) null else it },
                    onDismiss = { activeHistoryPt = null },
                )

                // Session list
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    // Empty state
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(Lucide.Clock, null, tint = TextTertiary.copy(alpha = 0.4f), modifier = Modifier.size(28.dp))
                        Text(
                            "No history yet",
                            style = MaterialTheme.typography.caption,
                            color = TextTertiary,
                        )
                    }
                }
            }
        }
    }

    // Log Set bottom sheet
    if (logSheetExId != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val exId = logSheetExId!!
        val ex = exercises.find { it.id == exId }

        ModalBottomSheet(
            onDismissRequest = { logSheetExId = null },
            sheetState = sheetState,
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
                Text(
                    ex?.name ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))

                // Weight
                AdjustRow(
                    label = "Weight (${ex?.unit ?: "kg"})",
                    display = if ((logWeight * 10).roundToInt() % 10 == 0) "${logWeight.toInt()}" else "%.1f".format(logWeight),
                    onDec = { logWeight = maxOf(0f, logWeight - 2.5f) },
                    onInc = { logWeight += 2.5f },
                )
                Spacer(Modifier.height(20.dp))

                // Reps
                AdjustRow(
                    label = "Reps",
                    display = "$logReps",
                    onDec = { if (logReps > 1) logReps-- },
                    onInc = { logReps++ },
                )
                Spacer(Modifier.height(28.dp))

                // Warmup toggle
                Row(
                    modifier = Modifier
                        .width(LogSetControlRowWidth)
                        .clickableNoRipple { isWarmup = !isWarmup }
                        .padding(vertical = 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Warmup set",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                    WarmupToggle(on = isWarmup, onClick = { isWarmup = !isWarmup })
                }
                Spacer(Modifier.height(28.dp))

                // Confirm
                WorkoutCtaButton(if (isWarmup) "Log Warmup" else "Log Set") {
                    val newSet = SetEntry(logWeight, logReps, isWarmup)
                    val prev = activeSets[exId] ?: emptyList()
                    activeSets = activeSets + (exId to (prev + newSet))
                    logSheetExId = null
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    "Cancel",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickableNoRipple { logSheetExId = null }
                        .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullscreenWorkoutSheet(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current
    val screenHeightPx = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val dismissThresholdPx = screenHeightPx / 3f
    var currentOffsetPx by remember { mutableFloatStateOf(0f) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { targetValue ->
            targetValue != SheetValue.Hidden || currentOffsetPx >= dismissThresholdPx
        },
    )

    LaunchedEffect(sheetState) {
        snapshotFlow { runCatching { sheetState.requireOffset() }.getOrNull() }
            .collect { offset ->
                if (offset != null) currentOffsetPx = offset
            }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(0.dp),
        containerColor = BgBase,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            content = content,
        )
    }
}

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
    ex: ExerciseDef,
    sets: List<SetEntry>,
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
            exerciseDetailsLine(ex),
            style = MaterialTheme.typography.exerciseDetails,
            color = TextTertiary,
            modifier = Modifier.padding(top = 4.dp),
        )

        if (sets.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                sets.forEach { s ->
                    val label = "${formatWeight(s.weight)} x ${s.reps}"
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
                        s.isPR -> {
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
    }
}

@Composable
private fun WorkoutCtaButton(label: String, onClick: () -> Unit) {
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

/**
 * Shows a labeled value control with decrement and increment buttons.
 *
 * @param label The control label.
 * @param display The value shown between the buttons.
 * @param onDec Called when the decrement button is pressed.
 * @param onInc Called when the increment button is pressed.
 */
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

/**
 * Renders a toggle switch for marking a set as warmup.
 *
 * @param on Whether the toggle is enabled.
 * @param onClick Called when the toggle is tapped.
 */
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
    activePt: Int?,
    onPtClick: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val today = LocalDate.now()
    val pts = (11 downTo 0).map { i ->
        val date = today.minusWeeks(i.toLong())
        val rm = 90f + (12 - i) * 4f + (Math.sin(i * 1.3) * 3).toFloat()
        Pair(date, rm)
    }

    val chartHeightDp = 110.dp
    val minRm = pts.minOf { it.second }
    val maxRm = pts.maxOf { it.second }
    val pad = (maxRm - minRm) * 0.14f
    val yMin = minRm - pad
    val yMax = maxRm + pad

    val tickStep = if (maxRm - minRm > 30) 20f else 10f
    val tLow = (Math.ceil(minRm / tickStep.toDouble()) * tickStep).toFloat()
    val tHigh = (Math.floor(maxRm / tickStep.toDouble()) * tickStep).toFloat()
    val tMid = (Math.round((tLow + tHigh) / 2f / tickStep) * tickStep).toFloat()
    val yTicks = if (tMid != tLow && tMid != tHigh) listOf(tHigh, tMid, tLow) else listOf(tHigh, tLow)

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
            "EST. 1RM OVER TIME",
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
                            .pointerInput(pts, yMin, yMax) {
                                val touchSlop = 36.dp.toPx()
                                detectTapGestures { offset ->
                                    val w = size.width.toFloat()
                                    val h = size.height.toFloat()
                                    val nearest = pts.indices.minByOrNull { i ->
                                        val x = (i.toFloat() / (pts.size - 1)) * w
                                        Math.abs(offset.x - x)
                                    }
                                    val onPoint = nearest != null && run {
                                        val x = (nearest.toFloat() / (pts.size - 1)) * w
                                        val y = h - ((pts[nearest].second - yMin) / (yMax - yMin)) * h
                                        Math.hypot((offset.x - x).toDouble(), (offset.y - y).toDouble()) <= touchSlop
                                    }
                                    if (onPoint) onPtClick(nearest!!) else onDismiss()
                                }
                            },
                    ) {
                        val w = size.width
                        val h = size.height
                        fun toX(i: Int) = (i.toFloat() / (pts.size - 1)) * w
                        fun toY(rm: Float) = h - ((rm - yMin) / (yMax - yMin)) * h
                        val coords = pts.indices.map { i -> Offset(toX(i), toY(pts[i].second)) }

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

                    if (activePt != null && activePt in pts.indices) {
                        val (date, rm) = pts[activePt]
                        val xFrac = activePt.toFloat() / (pts.size - 1)
                        val yFrac = 1f - (rm - yMin) / (yMax - yMin)
                        val dotXDp = maxWidth * xFrac
                        val dotYDp = maxHeight * yFrac
                        val tooltipDate = "${date.dayOfMonth} ${date.month.getDisplayName(JTextStyle.SHORT, Locale.ENGLISH)}"
                        val tooltipValue = "${rm.roundToInt()} kg"
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
                    pts.forEachIndexed { i, (date, _) ->
                        val key = "${date.year}-${date.monthValue}"
                        val existing = monthGroups[key]
                        monthGroups[key] = if (existing == null) i..i else existing.first..i
                    }
                    monthGroups.forEach { (_, range) ->
                        val centerFrac = (range.first + range.last).toFloat() / 2f / (pts.size - 1)
                        val label = pts[range.first].first.month.getDisplayName(JTextStyle.SHORT, Locale.ENGLISH)
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
