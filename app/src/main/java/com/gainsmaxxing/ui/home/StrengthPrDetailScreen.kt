package com.gainsmaxxing.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.gainsmaxxing.domain.SetComparison
import com.gainsmaxxing.domain.WeightFormat
import com.gainsmaxxing.domain.model.WeightUnit
import com.gainsmaxxing.ui.components.ChartTooltip
import com.gainsmaxxing.ui.components.clickableNoRipple
import com.gainsmaxxing.ui.theme.BgBase
import com.gainsmaxxing.ui.theme.BorderSubtle
import com.gainsmaxxing.ui.theme.Green500
import com.gainsmaxxing.ui.theme.Surface1
import com.gainsmaxxing.ui.theme.Surface2
import com.gainsmaxxing.ui.theme.Surface3
import com.gainsmaxxing.ui.theme.TextPrimary
import com.gainsmaxxing.ui.theme.TextSecondary
import com.gainsmaxxing.ui.theme.TextTertiary
import com.gainsmaxxing.ui.theme.caption
import com.gainsmaxxing.ui.theme.labelLargeCaps
import com.gainsmaxxing.ui.theme.monoBodyEmphasis
import com.gainsmaxxing.ui.theme.monoLabel
import com.gainsmaxxing.ui.theme.monoTitle
import com.gainsmaxxing.ui.theme.screenTitle
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt
import androidx.compose.foundation.Canvas

@Composable
fun StrengthPrDetailScreen(
    exerciseName: String,
    entries: List<StrengthPrEntryUi>,
    weightUnit: WeightUnit,
    onClose: () -> Unit,
    onLogOneRm: () -> Unit,
) {
    var activePt by remember { mutableStateOf<Int?>(null) }

    BackHandler(onBack = onClose)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                Text(exerciseName, style = MaterialTheme.typography.screenTitle, color = TextPrimary)
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))

            val sortedEntries = entries.sortedWith(
                compareBy<StrengthPrEntryUi>({ it.date }, { it.loggedAtEpochMs }),
            )
            val points = sortedEntries.map {
                it.date to WeightFormat.kgToDisplay(it.oneRmKg, weightUnit)
            }

            OneRmChart(
                points = points,
                weightUnit = weightUnit,
                activePt = activePt,
                onPtClick = { activePt = if (activePt == it) null else it },
                onDismiss = { activePt = null },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (entries.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(Lucide.Clock, null, tint = TextTertiary.copy(alpha = 0.4f), modifier = Modifier.size(28.dp))
                        Text("No 1RM entries yet", style = MaterialTheme.typography.caption, color = TextTertiary)
                    }
                } else {
                    sortedEntries.reversed().forEach { entry ->
                        StrengthPrEntryCard(entry)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Surface2)
                        .clickableNoRipple(onLogOneRm),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Lucide.Plus, null, tint = Green500, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

@Composable
private fun StrengthPrEntryCard(entry: StrengthPrEntryUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface1)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(entry.dateLabel, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                entry.oneRmDisplay,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.alignByBaseline(),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                entry.unitLabel,
                style = MaterialTheme.typography.monoLabel,
                color = TextTertiary,
                modifier = Modifier.alignByBaseline(),
            )
        }
    }
}

@Composable
private fun OneRmChart(
    points: List<Pair<LocalDate, Float>>,
    weightUnit: WeightUnit,
    activePt: Int?,
    onPtClick: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val chartHeightDp = 110.dp
    val unitLabel = WeightFormat.unitLabel(weightUnit)

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
        Text("1RM", style = MaterialTheme.typography.labelLargeCaps, color = TextTertiary)

        when {
            points.isEmpty() -> {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeightDp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Log a 1RM to get started",
                        style = MaterialTheme.typography.caption,
                        color = TextTertiary,
                        textAlign = TextAlign.Center,
                    )
                }
                return@Column
            }

            points.size == 1 -> {
                val weight = points.first().second
                val weightStr = if ((weight * 10).roundToInt() % 10 == 0) {
                    "${weight.toInt()}"
                } else {
                    "%.1f".format(Locale.ROOT, weight)
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = weightStr,
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
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeightDp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Add another entry to see the trend",
                        style = MaterialTheme.typography.caption,
                        color = TextTertiary,
                        textAlign = TextAlign.Center,
                    )
                }
                return@Column
            }
        }

        Spacer(Modifier.height(10.dp))

        val minWeight = points.minOf { it.second }
    val maxWeight = points.maxOf { it.second }
    val span = (maxWeight - minWeight).coerceAtLeast(2.5f)
    val pad = span * 0.14f
    val yMin = maxOf(0f, minWeight - pad)
    val yMax = maxWeight + pad

    val tickStep = SetComparison.weightTickStepKg(span)
    val tLow = (Math.ceil(yMin / tickStep.toDouble()) * tickStep).toFloat()
    val tHigh = (Math.floor(yMax / tickStep.toDouble()) * tickStep).toFloat()
    val yTicks = if (tLow > tHigh) {
        listOf((Math.round(minWeight / tickStep) * tickStep).toFloat())
    } else {
        val tMid = (Math.round((tLow + tHigh) / 2f / tickStep) * tickStep).toFloat()
        if (tMid != tLow && tMid != tHigh) listOf(tHigh, tMid, tLow) else listOf(tHigh, tLow)
    }

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
                        modifier = Modifier.offset { IntOffset(0, (topPx - 8.dp.toPx()).roundToInt()) },
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
                            if (activePt == i) {
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
                        val tooltipDate = "${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)}"
                        val tooltipValue = "${if ((weight * 10).roundToInt() % 10 == 0) weight.toInt() else "%.1f".format(weight)} ${WeightFormat.unitLabel(weightUnit)}"
                        ChartTooltip(
                            chartWidth = maxWidth,
                            chartHeight = maxHeight,
                            anchorX = maxWidth * xFrac,
                            anchorTop = maxHeight * yFrac,
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
            }
        }
    }
}
