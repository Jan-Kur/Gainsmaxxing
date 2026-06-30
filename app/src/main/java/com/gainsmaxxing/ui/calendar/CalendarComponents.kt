package com.gainsmaxxing.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.gainsmaxxing.domain.WeekMath
import com.gainsmaxxing.domain.model.CalendarActivityType
import com.gainsmaxxing.ui.components.clickableNoRipple
import com.gainsmaxxing.ui.theme.Green500
import com.gainsmaxxing.ui.theme.Surface2
import com.gainsmaxxing.ui.theme.TextDisabled
import com.gainsmaxxing.ui.theme.TextTertiary
import com.gainsmaxxing.ui.theme.monoSmall
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class CalendarSlotDisplay(
    val activityType: CalendarActivityType?,
    val isSkipped: Boolean = false,
    val onTap: (() -> Unit)? = null,
)

data class CalendarDayDisplay(
    val dayIndex: Int,
    val dayName: String = WeekMath.dayNames[dayIndex],
    val date: LocalDate? = null,
    val isToday: Boolean = false,
    val morning: CalendarSlotDisplay,
    val evening: CalendarSlotDisplay,
)

fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp = 10.dp,
    strokeWidth: Dp = 1.dp,
    dashLength: Dp = 5.dp,
    gapLength: Dp = 4.dp,
): Modifier = this.drawBehind {
    val strokePx = strokeWidth.toPx()
    val dashPx = dashLength.toPx()
    val gapPx = gapLength.toPx()
    drawRoundRect(
        color = color,
        style = Stroke(
            width = strokePx,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashPx, gapPx)),
        ),
        cornerRadius = CornerRadius(cornerRadius.toPx()),
    )
}

@Composable
fun CalendarWeekHeader(
    weekLabel: String?,
    onPreviousWeek: (() -> Unit)?,
    onNextWeek: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    if (weekLabel != null && onPreviousWeek != null && onNextWeek != null) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CalendarNavButton(icon = Lucide.ChevronLeft, onClick = onPreviousWeek)
            Text(
                text = weekLabel,
                style = MaterialTheme.typography.titleSmall,
                color = com.gainsmaxxing.ui.theme.TextSecondary,
            )
            CalendarNavButton(icon = Lucide.ChevronRight, onClick = onNextWeek)
        }
        Spacer(Modifier.height(8.dp))
    } else if (weekLabel != null) {
        Text(
            text = weekLabel,
            style = MaterialTheme.typography.titleSmall,
            color = com.gainsmaxxing.ui.theme.TextSecondary,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun CalendarSlotColumnsHeader(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Spacer(Modifier.width(42.dp))
        Text(
            "MORNING",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
        )
        Text(
            "EVENING",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
        )
    }
}

@Composable
fun CalendarWeekGrid(
    days: List<CalendarDayDisplay>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        days.forEach { day ->
            CalendarDayRow(
                day = day,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CalendarDayRow(
    day: CalendarDayDisplay,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CalendarDayLabel(
            dayName = day.dayName,
            dateNumber = day.date?.dayOfMonth,
            isToday = day.isToday,
        )
        ActivitySlot(
            activityType = day.morning.activityType,
            isSkipped = day.morning.isSkipped,
            onTap = day.morning.onTap,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        ActivitySlot(
            activityType = day.evening.activityType,
            isSkipped = day.evening.isSkipped,
            onTap = day.evening.onTap,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
    }
}

@Composable
private fun CalendarDayLabel(
    dayName: String,
    dateNumber: Int?,
    isToday: Boolean,
) {
    Column(
        modifier = Modifier
            .width(42.dp)
            .fillMaxHeight()
            .let {
                if (isToday) it.clip(RoundedCornerShape(10.dp)).background(Green500.copy(alpha = 0.06f))
                else it
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            dayName,
            style = MaterialTheme.typography.bodySmall,
            color = if (isToday) Green500 else TextTertiary,
        )
        if (dateNumber != null) {
            Text(
                "$dateNumber",
                style = MaterialTheme.typography.monoSmall,
                color = if (isToday) Green500 else TextDisabled,
            )
        }
    }
}

@Composable
fun CalendarNavButton(icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Surface2)
            .clickableNoRipple(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun ActivitySlot(
    activityType: CalendarActivityType?,
    isSkipped: Boolean,
    modifier: Modifier = Modifier,
    onTap: (() -> Unit)? = null,
) {
    if (activityType == null) {
        val clickableModifier = if (onTap != null) {
            Modifier.clickableNoRipple(onTap)
        } else {
            Modifier
        }
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(10.dp))
                .dashedBorder(
                    color = Color.White.copy(alpha = 0.10f),
                    cornerRadius = 10.dp,
                )
                .then(clickableModifier),
        )
        return
    }

    val baseColor = activityColor(activityType.colorPaletteIndex)
    val bgColor = if (isSkipped) Color.White.copy(alpha = 0.02f) else baseColor.copy(alpha = 0.11f)
    val borderColor = if (isSkipped) Color.White.copy(alpha = 0.05f) else baseColor.copy(alpha = 0.26f)
    val contentColor = if (isSkipped) TextDisabled else baseColor
    val icon = CalendarIcons.resolve(activityType.iconKey)

    val clickableModifier = if (onTap != null) {
        Modifier.clickableNoRipple(onTap)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .then(clickableModifier)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = activityType.name,
                tint = contentColor,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = activityType.name,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                textAlign = TextAlign.Center,
                textDecoration = if (isSkipped) TextDecoration.LineThrough else null,
            )
        }
    }
}

fun formatWeekRange(weekStart: LocalDate): String {
    val weekEnd = weekStart.plusDays(6)
    fun fmt(d: LocalDate) = "${d.dayOfMonth} ${d.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)}"
    return "${fmt(weekStart)} – ${fmt(weekEnd)}"
}

fun mondayOfWeekContaining(date: LocalDate): LocalDate {
    val dow = date.dayOfWeek.value
    return date.minusDays((dow - 1).toLong())
}
