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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.gainsmaxxing.ui.components.ChartTooltip
import com.gainsmaxxing.ui.components.clickableNoRipple
import com.gainsmaxxing.ui.home.SettingsSheet
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
import java.time.LocalDate
import java.time.format.TextStyle as JTextStyle
import java.util.Locale
import kotlin.math.roundToInt

data class PrEntry(val exercise: String, val value: String, val unit: String, val delta: String)
data class BwPoint(val date: LocalDate, val weight: Float)
data class SleepEntry(val date: LocalDate, val hours: Float, val energy: String)

private val strengthPRs = listOf(
    PrEntry("Bench Press", "102.5", "kg", "+2.5 kg"),
    PrEntry("Deadlift", "180", "kg", "+5 kg"),
    PrEntry("Back Squat", "140", "kg", "—"),
    PrEntry("Overhead Press", "72.5", "kg", "+2.5 kg"),
)

private val runningPRs = listOf(
    PrEntry("5 km", "22:48", "min", "−0:31"),
    PrEntry("10 km", "48:12", "min", "−1:20"),
    PrEntry("Half Mar.", "1:52", "h:mm", "First"),
    PrEntry("1 km", "4:02", "min", "−0:08"),
)

private fun generateBwData(): List<BwPoint> {
    val today = LocalDate.now()
    // Different shape than before: a shorter, recent ~30-week window with a
    // small weight range, so the Y-axis auto-scale and the 1 kg tick step are
    // exercised (vs. the wide 50-week / 2 kg-step range previously).
    val n = 30
    return (n - 1 downTo 0).map { i ->
        val date = today.minusWeeks(i.toLong())
        val trend = 73.3f + ((n - 1 - i).toFloat() / (n - 1)) * 3.1f
        val noise = Math.sin(i * 1.97) * 0.35 + Math.sin(i * 0.61) * 0.24
        BwPoint(date, ((trend + noise) * 10).roundToInt() / 10f)
    }
}

private fun generateSleepData(): List<SleepEntry> {
    val today = LocalDate.now()
    // Different shape than before: hits both extremes — values above the 9h
    // axis top (to test the bar clamp) and down at 4h — with a fuller energy mix.
    return (29 downTo 0).map { i ->
        val date = today.minusDays(i.toLong())
        val raw = 6.8 + Math.sin(i * 0.41) * 1.9 + Math.sin(i * 1.27) * 0.8
        val h = (raw * 2).roundToInt() / 2f
        val energy = when {
            h < 6.5f -> "Sleepy"
            h > 7.8f -> "Energised"
            else -> "Neutral"
        }
        SleepEntry(date, h, energy)
    }
}

@Composable
fun HomeScreen() {
    val bwData = remember { generateBwData() }
    val sleepData = remember { generateSleepData() }
    var prTab by rememberSaveable { mutableIntStateOf(0) }
    var activeBw by remember { mutableStateOf<Int?>(null) }
    var activeSleep by remember { mutableStateOf<Int?>(null) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    val hour = java.time.LocalTime.now().hour
    val greeting = if (hour < 12) "Good morning" else if (hour < 18) "Good afternoon" else "Good evening"

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
                        text = "Jan",
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
                        text = "J",
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
                    SectionLabel("Personal Records")
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

                    // 2×2 PR grid
                    val prs = if (prTab == 0) strengthPRs else runningPRs
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.height(220.dp),
                        userScrollEnabled = false,
                    ) {
                        items(prs) { pr ->
                            PrCard(pr)
                        }
                    }
                }

                // Bodyweight
                Column {
                    SectionLabel("Bodyweight")
                    Spacer(Modifier.height(12.dp))
                    BodyweightCard(
                        data = bwData.takeLast(26),
                        activeDot = activeBw,
                        onDotClick = { activeBw = if (activeBw == it) null else it },
                        onDismiss = { activeBw = null },
                    )
                }

                // Sleep
                Column {
                    SectionLabel("Sleep")
                    Spacer(Modifier.height(12.dp))
                    SleepCard(
                        data = sleepData,
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
            SettingsSheet(onClose = { showSettings = false })
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
private fun PrCard(pr: PrEntry) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Surface1)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text = pr.exercise.uppercase(),
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
            Spacer(Modifier.width(4.dp))
            Text(
                text = pr.unit,
                style = MaterialTheme.typography.monoLabel,
                color = TextTertiary,
                modifier = Modifier.alignByBaseline(),
            )
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
    data: List<BwPoint>,
    activeDot: Int?,
    onDotClick: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val current = data.last().weight
    // Change across the whole visible window, so the label matches the trend
    // the chart shows (week-over-week is too noisy to be meaningful).
    val delta = current - data.first().weight
    val deltaStr = (if (delta >= 0) "+" else "") + "%.1f kg".format(Locale.ROOT, delta)
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
                    text = "%.1f".format(Locale.ROOT, current),
                    style = MaterialTheme.typography.displaySmall,
                    color = TextPrimary,
                    modifier = Modifier.alignByBaseline(),
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = "kg",
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

        // Y-axis ticks + chart
        val chartHeightDp = 110.dp
        val weights = data.map { it.weight }
        val minW = weights.min()
        val maxW = weights.max()
        val pad = (maxW - minW) * 0.14f
        val yMin = minW - pad
        val yMax = maxW + pad

        val tickStep = if (maxW - minW > 6) 2f else 1f
        val tLow = (Math.ceil(minW / tickStep.toDouble()) * tickStep).toFloat()
        val tHigh = (Math.floor(maxW / tickStep.toDouble()) * tickStep).toFloat()
        val tMid = (Math.round((tLow + tHigh) / 2f / tickStep) * tickStep).toFloat()
        val yTicks = if (tMid != tLow && tMid != tHigh) listOf(tHigh, tMid, tLow) else listOf(tHigh, tLow)

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
                                        val x = (i.toFloat() / (data.size - 1)) * w
                                        Math.abs(offset.x - x)
                                    }
                                    val onPoint = nearest != null && run {
                                        val x = (nearest.toFloat() / (data.size - 1)) * w
                                        val y = h - ((data[nearest].weight - yMin) / (yMax - yMin)) * h
                                        Math.hypot((offset.x - x).toDouble(), (offset.y - y).toDouble()) <= touchSlop
                                    }
                                    if (onPoint) onDotClick(nearest!!) else onDismiss()
                                }
                            },
                    ) {
                        val w = size.width
                        val h = size.height
                        fun toX(i: Int) = (i.toFloat() / (data.size - 1)) * w
                        fun toY(weight: Float) = h - ((weight - yMin) / (yMax - yMin)) * h

                        val pts = data.indices.map { i -> Offset(toX(i), toY(data[i].weight)) }

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
                        val xFrac = activeDot.toFloat() / (data.size - 1)
                        val yFrac = 1f - (pt.weight - yMin) / (yMax - yMin)
                        val dotXDp = maxWidth * xFrac
                        val dotYDp = maxHeight * yFrac
                        val tooltipDate = "${pt.date.dayOfMonth} ${pt.date.month.getDisplayName(JTextStyle.SHORT, Locale.ENGLISH)}"
                        val tooltipWeight = "%.1f kg".format(Locale.ROOT, pt.weight)
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
                        val centerFrac = (range.first + range.last).toFloat() / 2f / (data.size - 1)
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
    val tickHours = listOf(4f, 5f, 6f, 7f, 8f)
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
                            val heightFrac = ((entry.hours - scaleMin) / span).coerceIn(0f, 1f)
                            val barColor = when (entry.energy) {
                                "Sleepy" -> SleepSleepy
                                "Neutral" -> SleepNeutral
                                else -> SleepEnergised
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
                                    Text("%.1f h".format(entry.hours), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                    val energyColor = when (entry.energy) {
                                        "Sleepy" -> Color(0xFF6BF5AD).copy(alpha = 0.5f)
                                        "Neutral" -> Green500.copy(alpha = 0.7f)
                                        else -> Green500
                                    }
                                    Text(entry.energy, style = MaterialTheme.typography.captionEmphasis, color = energyColor)
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
                    val labelCount = 5
                    val firstIdx = 3
                    val lastIdx = n - 4
                    val indices = (0 until labelCount)
                        .map { k -> (firstIdx + (lastIdx - firstIdx) * k / (labelCount - 1f)).roundToInt() }
                        .distinct()
                    indices.forEach { idx ->
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
