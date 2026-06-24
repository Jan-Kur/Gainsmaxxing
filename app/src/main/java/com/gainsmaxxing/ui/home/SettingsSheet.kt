package com.gainsmaxxing.ui.home

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.CalendarDays
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Dumbbell
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Scale
import com.gainsmaxxing.ui.components.clickableNoRipple
import com.gainsmaxxing.ui.theme.Amber500
import com.gainsmaxxing.ui.theme.BgBase
import com.gainsmaxxing.ui.theme.Blue500
import com.gainsmaxxing.ui.theme.BorderSubtle
import com.gainsmaxxing.ui.theme.Green500
import com.gainsmaxxing.ui.theme.Green700
import com.gainsmaxxing.ui.theme.Surface1
import com.gainsmaxxing.ui.theme.TextPrimary
import com.gainsmaxxing.ui.theme.TextSecondary
import com.gainsmaxxing.ui.theme.TextTertiary
import com.gainsmaxxing.ui.theme.caption
import com.gainsmaxxing.ui.theme.monoBodySmall
import com.gainsmaxxing.ui.theme.screenTitle

@Composable
fun SettingsSheet(onClose: () -> Unit) {
    var weightUnit by remember { mutableStateOf("kg") }
    var notificationsOn by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .statusBarsPadding(),
    ) {
        // Header
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
                text = "Settings",
                style = MaterialTheme.typography.screenTitle,
                color = TextPrimary,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp),
        ) {
            // Profile card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface1)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Green500, Green700))),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "J",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Jan",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                    )
                    Text(
                        text = "Edit profile",
                        style = MaterialTheme.typography.caption,
                        color = TextTertiary,
                    )
                }
                Icon(Lucide.ChevronRight, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
            }

            Spacer(Modifier.height(28.dp))

            SettingsSectionLabel("Training")
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface1)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
            ) {
                SettingsRow(icon = Lucide.Dumbbell, iconBg = Green500.copy(alpha = 0.12f), iconTint = Green500, label = "Edit Workout Split")
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(start = 58.dp).background(Color.White.copy(alpha = 0.06f)))
                SettingsRow(icon = Lucide.CalendarDays, iconBg = Blue500.copy(alpha = 0.12f), iconTint = Blue500, label = "Edit Calendar")
            }

            Spacer(Modifier.height(20.dp))
            SettingsSectionLabel("Preferences")
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface1)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickableNoRipple { weightUnit = if (weightUnit == "kg") "lbs" else "kg" }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Amber500.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Lucide.Scale, null, tint = Amber500, modifier = Modifier.size(15.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Weight Unit",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = weightUnit,
                        style = MaterialTheme.typography.monoBodySmall,
                        color = TextTertiary,
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(Lucide.ChevronRight, null, tint = TextTertiary, modifier = Modifier.size(15.dp))
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(start = 58.dp).background(Color.White.copy(alpha = 0.06f)))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickableNoRipple { notificationsOn = !notificationsOn }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.07f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Lucide.Bell, null, tint = TextSecondary, modifier = Modifier.size(15.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    ToggleSwitch(on = notificationsOn, onClick = { notificationsOn = !notificationsOn })
                }
            }

            Spacer(Modifier.height(20.dp))
            SettingsSectionLabel("Data")
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface1)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
            ) {
                SettingsRow(icon = Lucide.Download, iconBg = Color.White.copy(alpha = 0.07f), iconTint = TextSecondary, label = "Export Data")
            }
        }
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = TextTertiary,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun SettingsRow(icon: ImageVector, iconBg: Color, iconTint: Color, label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoRipple {}
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(15.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
        )
        Icon(Lucide.ChevronRight, null, tint = TextTertiary, modifier = Modifier.size(15.dp))
    }
}

@Composable
private fun ToggleSwitch(on: Boolean, onClick: () -> Unit) {
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
