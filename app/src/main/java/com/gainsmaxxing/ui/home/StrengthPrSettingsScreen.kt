package com.gainsmaxxing.ui.home

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Trash2
import com.gainsmaxxing.data.repository.StrengthPrRepository
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
fun StrengthPrSettingsScreen(
    initialNames: List<String>,
    onSave: (List<String>) -> Unit,
    onClose: () -> Unit,
) {
    val names = remember(initialNames) {
        mutableStateListOf<String>().apply {
            if (initialNames.isEmpty()) add("") else addAll(initialNames)
        }
    }

    BackHandler(onBack = onClose)

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
                text = "Strength Records",
                style = MaterialTheme.typography.screenTitle,
                color = TextPrimary,
            )
        }

        Text(
            text = "Choose up to ${StrengthPrRepository.MAX_SELECTION} exercises to show on Home.",
            style = MaterialTheme.typography.caption,
            color = TextTertiary,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            names.forEachIndexed { index, name ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Surface1)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicTextField(
                        value = name,
                        onValueChange = { names[index] = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                        cursorBrush = SolidColor(Green500),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (name.isEmpty()) {
                                Text("Exercise name", color = TextTertiary, style = MaterialTheme.typography.bodyMedium)
                            }
                            inner()
                        },
                    )
                    if (names.size > 1) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Lucide.Trash2,
                            contentDescription = "Remove",
                            tint = TextTertiary,
                            modifier = Modifier
                                .size(18.dp)
                                .clickableNoRipple { names.removeAt(index) },
                        )
                    }
                }
            }

            if (names.size < StrengthPrRepository.MAX_SELECTION) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Surface2)
                        .clickableNoRipple { names.add("") }
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Lucide.Plus, null, tint = Green500, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add exercise", style = MaterialTheme.typography.bodyMedium, color = Green500)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            com.gainsmaxxing.ui.workout.WorkoutCtaButton("Save") {
                onSave(names.map { it.trim() }.filter { it.isNotEmpty() })
            }
        }
    }
}
