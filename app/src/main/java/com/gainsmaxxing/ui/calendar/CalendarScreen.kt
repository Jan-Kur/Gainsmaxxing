package com.gainsmaxxing.ui.calendar

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Dumbbell
import com.composables.icons.lucide.Footprints
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Target
import com.composables.icons.lucide.Timer
import com.composables.icons.lucide.TrendingUp
import com.composables.icons.lucide.Waves
import com.composables.icons.lucide.Zap
import com.gainsmaxxing.ui.components.clickableNoRipple
import com.gainsmaxxing.ui.theme.BgBase
import com.gainsmaxxing.ui.theme.BorderSubtle
import com.gainsmaxxing.ui.theme.GeistFontFamily
import com.gainsmaxxing.ui.theme.GeistMonoFontFamily
import com.gainsmaxxing.ui.theme.Green500
import com.gainsmaxxing.ui.theme.Surface3
import com.gainsmaxxing.ui.theme.TextSecondary
import com.gainsmaxxing.ui.theme.TextTertiary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private data class ActivityType(val rgb: Triple<Int, Int, Int>, val icon: ImageVector, val label: String)

private val activityTypes = mapOf(
    "long-run" to ActivityType(Triple(255, 167, 38), Lucide.Footprints, "Long Run"),
    "short-run" to ActivityType(Triple(255, 167, 38), Lucide.Timer, "Short Run"),
    "tempo-run" to ActivityType(Triple(255, 167, 38), Lucide.TrendingUp, "Tempo Run"),
    "sprinting" to ActivityType(Triple(255, 167, 38), Lucide.Zap, "Sprint"),
    "gym" to ActivityType(Triple(0, 230, 118), Lucide.Dumbbell, "Gym"),
    "swimming" to ActivityType(Triple(61, 155, 255), Lucide.Waves, "Swim"),
    "basketball" to ActivityType(Triple(255, 140, 66), Lucide.Target, "Basketball"),
)

// Mon=0 … Sun=6
private val schedule = listOf(
    Pair("short-run", "gym"),
    Pair(null, "swimming"),
    Pair("tempo-run", "gym"),
    Pair(null, "basketball"),
    Pair("long-run", null),
    Pair("gym", null),
    Pair(null, null),
)

@Composable
fun CalendarScreen() {
    var weekOffset by rememberSaveable { mutableIntStateOf(0) }
    var skipped by rememberSaveable { mutableStateOf(setOf("0-3-evening", "0-4-morning", "-1-1-evening", "-1-5-morning")) }

    val today = LocalDate.now()
    // Find Monday of current week (our Mon=0 system)
    val dow = today.dayOfWeek.value // 1=Mon..7=Sun
    val monday = today.minusDays((dow - 1).toLong())
    val weekStart = monday.plusWeeks(weekOffset.toLong())
    val weekEnd = weekStart.plusDays(6)

    fun fmtRange(d: LocalDate) = "${d.dayOfMonth} ${d.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)}"
    val weekLabel = "${fmtRange(weekStart)} – ${fmtRange(weekEnd)}"

    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        // Week navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavButton(icon = Lucide.ChevronLeft) { weekOffset-- }
            Text(
                text = weekLabel,
                fontFamily = GeistFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = TextSecondary,
                letterSpacing = 0.13.sp,
            )
            NavButton(icon = Lucide.ChevronRight) { weekOffset++ }
        }

        Spacer(Modifier.height(8.dp))

        // Column headers
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Spacer(Modifier.width(38.dp))
            Text(
                "MORNING",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontFamily = GeistFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 1.08.sp,
                color = TextTertiary,
            )
            Text(
                "EVENING",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontFamily = GeistFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 1.08.sp,
                color = TextTertiary,
            )
        }

        Spacer(Modifier.height(8.dp))

        // Day rows
        Column(
            modifier = Modifier.weight(1f).padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            (0..6).forEach { dayIdx ->
                val date = weekStart.plusDays(dayIdx.toLong())
                val isToday = date == today
                val (mornType, evType) = schedule[dayIdx]
                val mornKey = "$weekOffset-$dayIdx-morning"
                val evKey = "$weekOffset-$dayIdx-evening"

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .let {
                            if (isToday) it.clip(RoundedCornerShape(10.dp)).background(Green500.copy(alpha = 0.04f))
                            else it
                        },
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Day label
                    Column(
                        modifier = Modifier.width(38.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            dayNames[dayIdx],
                            fontFamily = GeistFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = if (isToday) Green500 else TextSecondary,
                            lineHeight = 11.sp,
                        )
                        Text(
                            "${date.dayOfMonth}",
                            fontFamily = GeistMonoFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            color = if (isToday) Green500 else TextTertiary,
                            lineHeight = 11.sp,
                        )
                    }

                    // Morning slot
                    ActivitySlot(
                        type = mornType,
                        isSkipped = skipped.contains(mornKey),
                        modifier = Modifier.weight(1f),
                        onTap = {
                            skipped = if (mornType != null) {
                                if (skipped.contains(mornKey)) skipped - mornKey else skipped + mornKey
                            } else skipped
                        },
                    )

                    // Evening slot
                    ActivitySlot(
                        type = evType,
                        isSkipped = skipped.contains(evKey),
                        modifier = Modifier.weight(1f),
                        onTap = {
                            skipped = if (evType != null) {
                                if (skipped.contains(evKey)) skipped - evKey else skipped + evKey
                            } else skipped
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun NavButton(icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Surface3)
            .clickableNoRipple(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun ActivitySlot(
    type: String?,
    isSkipped: Boolean,
    modifier: Modifier = Modifier,
    onTap: () -> Unit,
) {
    if (type == null) {
        Box(
            modifier = modifier
                .height(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
        )
        return
    }

    val cfg = activityTypes[type] ?: return
    val (r, g, b) = cfg.rgb
    val baseColor = Color(r / 255f, g / 255f, b / 255f, 1f)

    val bgColor = if (isSkipped) Color.White.copy(alpha = 0.03f) else baseColor.copy(alpha = 0.11f)
    val borderColor = if (isSkipped) Color.White.copy(alpha = 0.07f) else baseColor.copy(alpha = 0.26f)
    val contentColor = if (isSkipped) TextTertiary else baseColor

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .then(if (isSkipped) Modifier.then(Modifier) else Modifier)
            .clickableNoRipple(onTap)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = cfg.icon,
                contentDescription = cfg.label,
                tint = contentColor.let { if (isSkipped) it.copy(alpha = 0.5f) else it },
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = cfg.label,
                fontFamily = GeistFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                color = contentColor.let { if (isSkipped) it.copy(alpha = 0.5f) else it },
                textAlign = TextAlign.Center,
                textDecoration = if (isSkipped) TextDecoration.LineThrough else null,
                lineHeight = 10.sp,
            )
        }
    }
}
