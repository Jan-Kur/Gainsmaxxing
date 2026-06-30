package com.gainsmaxxing.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.gainsmaxxing.domain.SleepFormat
import com.gainsmaxxing.domain.WeightFormat
import com.gainsmaxxing.domain.model.BodyweightEntry
import com.gainsmaxxing.domain.model.EnergyTag
import com.gainsmaxxing.domain.model.SleepEntry
import com.gainsmaxxing.domain.model.WeightUnit
import com.gainsmaxxing.ui.components.clickableNoRipple
import com.gainsmaxxing.ui.workout.SplitEditorScreen
import com.gainsmaxxing.ui.theme.Amber500
import com.gainsmaxxing.ui.theme.BgBase
import com.gainsmaxxing.ui.theme.Blue500
import com.gainsmaxxing.ui.theme.BorderSubtle
import com.gainsmaxxing.ui.theme.caption
import com.gainsmaxxing.ui.theme.captionEmphasis
import com.gainsmaxxing.ui.theme.labelLargeCaps
import com.gainsmaxxing.ui.theme.monoBodyEmphasis
import com.gainsmaxxing.ui.theme.monoLabel
import com.gainsmaxxing.ui.theme.monoTitle
import com.gainsmaxxing.ui.theme.Green500
import com.gainsmaxxing.ui.theme.Green700
import com.gainsmaxxing.ui.theme.SleepEnergised
import com.gainsmaxxing.ui.theme.SleepNeutral
import com.gainsmaxxing.ui.theme.SleepSleepy
import com.gainsmaxxing.ui.theme.Surface1
import com.gainsmaxxing.ui.theme.Surface2
import com.gainsmaxxing.ui.theme.Surface3
import com.gainsmaxxing.ui.theme.Surface4
import com.gainsmaxxing.ui.theme.TextPrimary
import com.gainsmaxxing.ui.theme.TextSecondary
import com.gainsmaxxing.ui.theme.TextTertiary
import com.gainsmaxxing.ui.components.ChartTooltip
import java.time.LocalDate
import java.time.format.TextStyle as JTextStyle
import java.util.Locale
import kotlin.math.roundToInt


private const val PrCardRowHeightDp = 105
private const val PrGridRowGapDp = 10

private fun prGridHeight(cardCount: Int) =
    (PrCardRowHeightDp * ((cardCount + 1) / 2) +
        PrGridRowGapDp * (((cardCount + 1) / 2) - 1).coerceAtLeast(0)).dp

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showStrengthPrSettings by viewModel.showStrengthPrSettings.collectAsStateWithLifecycle()
    val profile = uiState.profile

    var prTab by rememberSaveable { mutableIntStateOf(0) }
    var activeBw by remember { mutableStateOf<Int?>(null) }
    var activeSleep by remember { mutableStateOf<Int?>(null) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showSplitEditor by rememberSaveable { mutableStateOf(false) }
    var showBodyweightLog by remember { mutableStateOf(false) }
    var showSleepLog by remember { mutableStateOf(false) }
    var showStrengthPrLog by remember { mutableStateOf(false) }

    val hour = java.time.LocalTime.now().hour
    val greeting = if (hour < 12) "Good morning" else if (hour < 18) "Good afternoon" else "Good evening"

    val latestBwKg = uiState.bodyweightEntries.lastOrNull()?.weightKg
    val bwLogDefaultKg = latestBwKg ?: 70f
    val prLogDefaultKg = uiState.detailEntries.maxByOrNull { it.date }?.oneRmKg ?: 60f

    Box(modifier = Modifier.fillMaxSize().background(BgBase)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Fixed header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                    )
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Green500, Green700)))
                        .clickableNoRipple { showSettings = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = profile.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                    )
                }
            }

            // Scrollable body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // Personal Records
                Column {
                    SectionHeader("Personal Records")
                    Spacer(Modifier.height(12.dp))

                    // Strength/Running switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Surface2)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        listOf("Strength", "Running").forEachIndexed { i, label ->
                            val active = prTab == i
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(if (active) Surface4 else Color.Transparent)
                                    .clickableNoRipple { prTab = i }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (active) TextPrimary else TextTertiary,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    if (prTab == 0) {
                        if (uiState.strengthPrCards.isEmpty()) {
                            EmptyPrState(
                                message = "Add exercises in Settings → Strength Records",
                            )
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.height(prGridHeight(uiState.strengthPrCards.size)),
                                userScrollEnabled = false,
                            ) {
                                items(uiState.strengthPrCards) { pr ->
                                    PrCard(
                                        pr = pr,
                                        onClick = { viewModel.openStrengthPrDetail(pr.exerciseName) },
                                    )
                                }
                            }
                        }
                    } else {
                        EmptyPrState(message = "Running records will sync from Strava")
                    }
                }

                // Bodyweight
                Column {
                    SectionHeader(
                        title = "Bodyweight",
                        onAdd = { showBodyweightLog = true },
                    )
                    Spacer(Modifier.height(12.dp))
                    BodyweightCard(
                        data = uiState.bodyweightEntries,
                        weightUnit = profile.weightUnit,
                        activeDot = activeBw,
                        onDotClick = { activeBw = if (activeBw == it) null else it },
                        onDismiss = { activeBw = null },
                    )
                }

                // Sleep
                Column {
                    SectionHeader(
                        title = "Sleep",
                        onAdd = { showSleepLog = true },
                    )
                    Spacer(Modifier.height(12.dp))
                    SleepCard(
                        data = uiState.sleepEntries,
                        activeBar = activeSleep,
                        onBarClick = { activeSleep = if (activeSleep == it) null else it },
                        onDismiss = { activeSleep = null },
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showSettings,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.fillMaxSize(),
        ) {
            SettingsSheet(
                profile = profile,
                onClose = { showSettings = false },
                onEditSplit = {
                    showSettings = false
                    showSplitEditor = true
                },
                onEditStrengthPrs = {
                    showSettings = false
                    viewModel.openStrengthPrSettings()
                },
                onToggleWeightUnit = viewModel::toggleWeightUnit,
                onProfileNameChange = viewModel::setProfileName,
            )
        }

        AnimatedVisibility(
            visible = showSplitEditor,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.fillMaxSize(),
        ) {
            SplitEditorScreen(onClose = { showSplitEditor = false })
        }

        AnimatedVisibility(
            visible = showStrengthPrSettings,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.fillMaxSize(),
        ) {
            StrengthPrSettingsScreen(
                onClose = viewModel::closeStrengthPrSettings,
            )
        }

        if (uiState.detailExerciseName != null) {
            StrengthPrDetailScreen(
                exerciseName = uiState.detailExerciseName!!,
                entries = uiState.detailEntries,
                weightUnit = profile.weightUnit,
                onClose = viewModel::closeStrengthPrDetail,
                onLogOneRm = { showStrengthPrLog = true },
            )
        }

        if (showBodyweightLog) {
            BodyweightLogSheet(
                weightUnit = profile.weightUnit,
                initialDisplayKg = bwLogDefaultKg,
                onDismiss = { showBodyweightLog = false },
                onSave = { kg ->
                    viewModel.logBodyweight(kg)
                    showBodyweightLog = false
                },
            )
        }

        if (showSleepLog) {
            SleepLogSheet(
                onDismiss = { showSleepLog = false },
                onSave = { hours, tag ->
                    viewModel.logSleep(hours, tag)
                    showSleepLog = false
                },
            )
        }

        if (showStrengthPrLog && uiState.detailExerciseName != null) {
            StrengthPrLogSheet(
                exerciseName = uiState.detailExerciseName!!,
                weightUnit = profile.weightUnit,
                initialDisplayKg = prLogDefaultKg,
                onDismiss = { showStrengthPrLog = false },
                onSave = { kg ->
                    viewModel.logStrengthPr(kg)
                    showStrengthPrLog = false
                },
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = TextTertiary,
    )
}

@Composable
private fun SectionHeader(title: String, onAdd: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SectionLabel(title)
        if (onAdd != null) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
                    .clip(CircleShape)
                    .clickableNoRipple(onAdd),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Lucide.Plus, contentDescription = "Add", tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun EmptyPrState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(prGridHeight(1))
            .clip(RoundedCornerShape(16.dp))
            .background(Surface1)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary,
            modifier = Modifier.padding(horizontal = 24.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PrCard(pr: PrCardUi, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Surface1)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .clickableNoRipple(onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text = pr.exerciseName.uppercase(),
            style = MaterialTheme.typography.labelLargeCaps,
            color = TextTertiary,
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = pr.value,
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                modifier = Modifier.alignByBaseline(),
            )
            if (pr.unit.isNotEmpty()) {
                Spacer(Modifier.width(4.dp))
                Text(
                    text = pr.unit,
                    style = MaterialTheme.typography.monoLabel,
                    color = TextTertiary,
                    modifier = Modifier.alignByBaseline(),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        val deltaColor = when {
            pr.delta.startsWith("−") || pr.delta.startsWith("-") -> Green500
            pr.delta.startsWith("+") -> Green500
            else -> TextTertiary
        }
        Text(
            text = pr.delta,
            style = MaterialTheme.typography.labelLarge,
            color = deltaColor,
        )
    }
}

@Composable
private fun BodyweightCard(
    data: List<BodyweightEntry>,
    weightUnit: WeightUnit,
    activeDot: Int?,
    onDotClick: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val unitLabel = WeightFormat.unitLabel(weightUnit)
    val latest = data.lastOrNull()
    val currentDisplay = latest?.let { WeightFormat.kgToDisplay(it.weightKg, weightUnit) }
    val deltaStr = if (data.size >= 2) {
        val deltaKg = data.last().weightKg - data.first().weightKg
        val deltaDisplay = WeightFormat.kgToDisplay(deltaKg, weightUnit)
        val sign = if (deltaDisplay >= 0) "+" else ""
        val valueStr = if ((deltaDisplay * 10).roundToInt() % 10 == 0) {
            "${deltaDisplay.toInt()}"
        } else {
            "%.1f".format(Locale.ROOT, deltaDisplay)
        }
        "$sign$valueStr $unitLabel"
    } else {
        "—"
    }

    val deltaColor = Green500

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Surface1)
            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
            .pointerInput(Unit) { detectTapGestures { onDismiss() } }
            .padding(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = currentDisplay?.let {
                        if ((it * 10).roundToInt() % 10 == 0) "${it.toInt()}" else "%.1f".format(Locale.ROOT, it)
                    } ?: "—",
                    style = MaterialTheme.typography.displaySmall,
                    color = TextPrimary,
                    modifier = Modifier.alignByBaseline(),
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = unitLabel,
                    style = MaterialTheme.typography.monoTitle,
                    color = TextTertiary,
                    modifier = Modifier.alignByBaseline(),
                )
            }
            Text(
                text = deltaStr,
                style = MaterialTheme.typography.labelLarge,
                color = deltaColor,
            )
        }

        Spacer(Modifier.height(16.dp))

        if (data.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("No weigh-ins yet", style = MaterialTheme.typography.caption, color = TextTertiary)
            }
            return@Column
        }

        // Y-axis ticks + chart
        val chartHeightDp = 110.dp
        val weights = data.map { WeightFormat.kgToDisplay(it.weightKg, weightUnit) }
        val minW = weights.min()
        val maxW = weights.max()
        val pad = ((maxW - minW).coerceAtLeast(0.5f)) * 0.14f
        val yMin = minW - pad
        val yMax = maxW + pad

        val tickStep = if (maxW - minW > 6) 2f else 1f
        val tLow = (Math.ceil(yMin / tickStep.toDouble()) * tickStep).toFloat()
        val tHigh = (Math.floor(yMax / tickStep.toDouble()) * tickStep).toFloat()
        val tMid = (Math.round((tLow + tHigh) / 2f / tickStep) * tickStep).toFloat()
        val yTicks = if (tMid != tLow && tMid != tHigh) listOf(tHigh, tMid, tLow) else listOf(tHigh, tLow)
        val xDenom = (data.size - 1).coerceAtLeast(1)

        Row(verticalAlignment = Alignment.Top) {
            // Y axis
            Box(modifier = Modifier.wrapContentWidth().height(chartHeightDp)) {
                yTicks.forEach { tick ->
                    val topFrac = (1f - (tick - yMin) / (yMax - yMin))
                    val density = LocalDensity.current
                    val topPx = with(density) { chartHeightDp.toPx() * topFrac }
                    Text(
                        text = "${tick.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        modifier = Modifier
                            .offset { IntOffset(0, (topPx - 8.dp.toPx()).roundToInt()) },
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Chart canvas
                BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(chartHeightDp)) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(data, yMin, yMax) {
                                val touchSlop = 36.dp.toPx()
                                detectTapGestures { offset ->
                                    val w = size.width.toFloat()
                                    val h = size.height.toFloat()
                                    val nearest = data.indices.minByOrNull { i ->
                                        val x = (i.toFloat() / xDenom) * w
                                        Math.abs(offset.x - x)
                                    }
                                    val onPoint = nearest != null && run {
                                        val x = (nearest.toFloat() / xDenom) * w
                                        val y = h - ((weights[nearest] - yMin) / (yMax - yMin)) * h
                                        Math.hypot((offset.x - x).toDouble(), (offset.y - y).toDouble()) <= touchSlop
                                    }
                                    if (onPoint) onDotClick(nearest!!) else onDismiss()
                                }
                            },
                    ) {
                        val w = size.width
                        val h = size.height
                        fun toX(i: Int) = (i.toFloat() / xDenom) * w
                        fun toY(weight: Float) = h - ((weight - yMin) / (yMax - yMin)) * h

                        val pts = data.indices.map { i -> Offset(toX(i), toY(weights[i])) }

                        // Gridlines
                        yTicks.forEach { tick ->
                            drawLine(
                                color = Color(0x0FFFFFFF),
                                start = Offset(0f, toY(tick)),
                                end = Offset(w, toY(tick)),
                                strokeWidth = 1.dp.toPx(),
                            )
                        }

                        // Area fill
                        val linePath = Path()
                        linePath.moveTo(pts[0].x, pts[0].y)
                        for (i in 1 until pts.size) {
                            val prev = pts[i - 1]
                            val cur = pts[i]
                            val cx = (prev.x + cur.x) / 2f
                            linePath.cubicTo(cx, prev.y, cx, cur.y, cur.x, cur.y)
                        }
                        val areaPath = Path().apply {
                            addPath(linePath)
                            lineTo(pts.last().x, h)
                            lineTo(0f, h)
                            close()
                        }
                        drawPath(
                            areaPath,
                            brush = Brush.verticalGradient(
                                listOf(Green500.copy(alpha = 0.16f), Green500.copy(alpha = 0f)),
                                startY = 0f, endY = h,
                            ),
                        )
                        // Line
                        drawPath(
                            linePath,
                            color = Green500,
                            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                        )
                        // Dots
                        pts.forEachIndexed { i, p ->
                            val isActive = activeDot == i
                            if (isActive) {
                                drawCircle(Green500, radius = 7.dp.toPx(), center = p)
                                drawCircle(Color.White, radius = 4.dp.toPx(), center = p)
                            } else {
                                drawCircle(Green500, radius = 1.8.dp.toPx(), center = p)
                            }
                        }
                    }

                    // Tooltip
                    if (activeDot != null && activeDot in data.indices) {
                        val pt = data[activeDot]
                        val displayWeight = weights[activeDot]
                        val xFrac = activeDot.toFloat() / xDenom
                        val yFrac = 1f - (displayWeight - yMin) / (yMax - yMin)
                        val dotXDp = maxWidth * xFrac
                        val dotYDp = maxHeight * yFrac
                        val tooltipDate = "${pt.date.dayOfMonth} ${pt.date.month.getDisplayName(JTextStyle.SHORT, Locale.ENGLISH)}"
                        val tooltipWeight = "${WeightFormat.formatWeight(pt.weightKg, weightUnit)} $unitLabel"
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
                                    Text(tooltipWeight, style = MaterialTheme.typography.monoBodyEmphasis, color = TextPrimary)
                                }
                            }
                        }
                    }
                }

                // Month labels: one per month, centred on that month's span and
                // allowed to overflow (so the first/last month never clips).
                Spacer(Modifier.height(6.dp))
                BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(16.dp)) {
                    val totalWidth = maxWidth
                    val monthGroups = LinkedHashMap<String, IntRange>()
                    data.forEachIndexed { i, pt ->
                        val key = "${pt.date.year}-${pt.date.monthValue}"
                        val existing = monthGroups[key]
                        monthGroups[key] = if (existing == null) i..i else existing.first..i
                    }
                    monthGroups.forEach { (_, range) ->
                        val centerFrac = (range.first + range.last).toFloat() / 2f / xDenom
                        val label = data[range.first].date.month.getDisplayName(JTextStyle.SHORT, Locale.ENGLISH)
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

@Composable
private fun SleepCard(
    data: List<SleepEntry>,
    activeBar: Int?,
    onBarClick: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val scaleMin = 3.5f
    val scaleMax = 9f
    val span = scaleMax - scaleMin
    val tickHours = listOf(4f, 5f, 6f, 7f, 8f, 9f)
    val chartHeightDp = 130.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Surface1)
            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
            .pointerInput(Unit) { detectTapGestures { onDismiss() } }
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // Y axis
            Box(modifier = Modifier.wrapContentWidth().height(chartHeightDp)) {
                tickHours.forEach { h ->
                    val topFrac = 1f - (h - scaleMin) / span
                    val density = LocalDensity.current
                    val topPx = with(density) { chartHeightDp.toPx() * topFrac }
                    Text(
                        text = "${h.toInt()}h",
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
                    // Gridlines
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        tickHours.forEach { tick ->
                            val y = h - ((tick - scaleMin) / span) * h
                            drawLine(
                                color = Color(0x12FFFFFF),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1.dp.toPx(),
                            )
                        }
                    }

                    // Bars + tap targets
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        data.forEachIndexed { i, entry ->
                            if (entry.hours <= 0f) {
                                Spacer(modifier = Modifier.weight(1f))
                                return@forEachIndexed
                            }
                            val heightFrac = ((entry.hours - scaleMin) / span).coerceIn(0f, 1f)
                            val barColor = when (entry.energyTag) {
                                EnergyTag.SLEEPY -> SleepSleepy
                                EnergyTag.NEUTRAL -> SleepNeutral
                                EnergyTag.ENERGISED -> SleepEnergised
                            }
                            val alpha = if (activeBar == null || activeBar == i) 1f else 0.28f
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(heightFrac)
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(barColor.copy(alpha = alpha))
                                    .clickableNoRipple { onBarClick(i) },
                            )
                        }
                    }

                    // Tooltip
                    if (activeBar != null && activeBar in data.indices) {
                        val entry = data[activeBar]
                        if (entry.hours > 0f) {
                        val xFrac = (activeBar + 0.5f) / data.size
                        val barHeightFrac = maxOf(0f, (entry.hours - scaleMin) / span)
                        val barTopYDp = maxHeight * (1f - barHeightFrac)
                        val dotXDp = maxWidth * xFrac
                        ChartTooltip(
                            chartWidth = maxWidth,
                            chartHeight = maxHeight,
                            anchorX = dotXDp,
                            anchorTop = barTopYDp,
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Surface3)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Column {
                                    val label = "${entry.date.dayOfMonth} ${entry.date.month.getDisplayName(JTextStyle.SHORT, Locale.ENGLISH)}"
                                    Text(label, style = MaterialTheme.typography.caption, color = TextTertiary)
                                    Text(
                                        SleepFormat.formatDuration(entry.hours),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                    )
                                    val energyColor = when (entry.energyTag) {
                                        EnergyTag.SLEEPY -> Color(0xFF6BF5AD).copy(alpha = 0.5f)
                                        EnergyTag.NEUTRAL -> Green500.copy(alpha = 0.7f)
                                        EnergyTag.ENERGISED -> Green500
                                    }
                                    Text(entry.energyTag.displayName, style = MaterialTheme.typography.captionEmphasis, color = energyColor)
                                }
                            }
                        }
                        }
                    }
                }

                // X-axis labels: inset from the edges and evenly spaced, each
                // centred on its bar (so they never clip at the chart ends).
                Spacer(Modifier.height(5.dp))
                BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(14.dp)) {
                    val totalWidth = maxWidth
                    val n = data.size
                    val labelIndices = listOf(0, 7, 14, 21, 29).filter { it < n }
                    labelIndices.forEach { idx ->
                        val entry = data[idx]
                        val centerFrac = (idx + 0.5f) / n
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = totalWidth * centerFrac)
                                .width(0.dp)
                                .wrapContentWidth(unbounded = true),
                        ) {
                            Text(
                                text = "${entry.date.dayOfMonth} ${entry.date.month.getDisplayName(JTextStyle.SHORT, Locale.ENGLISH)}",
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

        // Legend
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            listOf("Sleepy" to SleepSleepy, "Neutral" to SleepNeutral, "Energised" to SleepEnergised).forEach { (label, color) ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(color))
                    Text(label, style = MaterialTheme.typography.caption, color = TextTertiary)
                }
            }
        }
    }
}
