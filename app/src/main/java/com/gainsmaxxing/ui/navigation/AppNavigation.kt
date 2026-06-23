package com.gainsmaxxing.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.CalendarDays
import com.composables.icons.lucide.Dumbbell
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.gainsmaxxing.ui.calendar.CalendarScreen
import com.gainsmaxxing.ui.components.clickableNoRipple
import com.gainsmaxxing.ui.home.HomeScreen
import com.gainsmaxxing.ui.theme.BorderSubtle
import com.gainsmaxxing.ui.theme.GeistFontFamily
import com.gainsmaxxing.ui.theme.Green500
import com.gainsmaxxing.ui.theme.TextTertiary
import com.gainsmaxxing.ui.workout.WorkoutScreen

private data class TabItem(val label: String, val icon: ImageVector)

private val tabs = listOf(
    TabItem("Calendar", Lucide.CalendarDays),
    TabItem("Home", Lucide.House),
    TabItem("Workout", Lucide.Dumbbell),
)

@Composable
fun AppNavigation() {
    var selectedTab by rememberSaveable { mutableIntStateOf(1) } // Home default

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            val borderColor = BorderSubtle
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xF0111316))
                    .drawBehind {
                        drawLine(
                            color = borderColor,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }
                    .navigationBarsPadding(),
            ) {
                Row(modifier = Modifier.fillMaxWidth().height(72.dp)) {
                    tabs.forEachIndexed { index, tab ->
                        val selected = selectedTab == index
                        val tint = if (selected) Green500 else TextTertiary
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clickableNoRipple { selectedTab = index },
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    tint = tint,
                                    modifier = Modifier.size(22.dp),
                                )
                                Text(
                                    text = tab.label,
                                    color = tint,
                                    fontFamily = GeistFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> CalendarScreen()
                1 -> HomeScreen()
                2 -> WorkoutScreen()
            }
        }
    }
}
