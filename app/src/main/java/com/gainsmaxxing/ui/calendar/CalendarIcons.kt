package com.gainsmaxxing.ui.calendar

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.Activity
import com.composables.icons.lucide.Bike
import com.composables.icons.lucide.Dumbbell
import com.composables.icons.lucide.Flame
import com.composables.icons.lucide.Footprints
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Medal
import com.composables.icons.lucide.Mountain
import com.composables.icons.lucide.MountainSnow
import com.composables.icons.lucide.PersonStanding
import com.composables.icons.lucide.Route
import com.composables.icons.lucide.Sailboat
import com.composables.icons.lucide.Snowflake
import com.composables.icons.lucide.SquareActivity
import com.composables.icons.lucide.StretchHorizontal
import com.composables.icons.lucide.Target
import com.composables.icons.lucide.Timer
import com.composables.icons.lucide.TimerReset
import com.composables.icons.lucide.TrendingUp
import com.composables.icons.lucide.Trophy
import com.composables.icons.lucide.Waves
import com.composables.icons.lucide.Zap

data class CuratedCalendarIcon(
    val key: String,
    val icon: ImageVector,
    val label: String,
)

object CalendarIcons {
    const val DEFAULT_KEY = "dumbbell"

    val curated: List<CuratedCalendarIcon> = listOf(
        CuratedCalendarIcon("sport-shoe", LucideSportShoe, "Running"),
        CuratedCalendarIcon("basketball", LucideBasketball, "Basketball"),
        CuratedCalendarIcon("footprints", Lucide.Footprints, "Walk"),
        CuratedCalendarIcon("timer", Lucide.Timer, "Timer"),
        CuratedCalendarIcon("trending-up", Lucide.TrendingUp, "Tempo"),
        CuratedCalendarIcon("zap", Lucide.Zap, "Sprint"),
        CuratedCalendarIcon("dumbbell", Lucide.Dumbbell, "Weights"),
        CuratedCalendarIcon("waves", Lucide.Waves, "Swim"),
        CuratedCalendarIcon("bike", Lucide.Bike, "Cycle"),
        CuratedCalendarIcon("mountain", Lucide.Mountain, "Hike"),
        CuratedCalendarIcon("mountain-snow", Lucide.MountainSnow, "Ski"),
        CuratedCalendarIcon("person-standing", Lucide.PersonStanding, "Sport"),
        CuratedCalendarIcon("activity", Lucide.Activity, "Cardio"),
        CuratedCalendarIcon("flame", Lucide.Flame, "HIIT"),
        CuratedCalendarIcon("heart", Lucide.Heart, "Endurance"),
        CuratedCalendarIcon("route", Lucide.Route, "Route"),
        CuratedCalendarIcon("sailboat", Lucide.Sailboat, "Row"),
        CuratedCalendarIcon("trophy", Lucide.Trophy, "Compete"),
        CuratedCalendarIcon("medal", Lucide.Medal, "Medal"),
        CuratedCalendarIcon("target", Lucide.Target, "Target"),
        CuratedCalendarIcon("square-activity", Lucide.SquareActivity, "Workout"),
        CuratedCalendarIcon("snowflake", Lucide.Snowflake, "Winter"),
        CuratedCalendarIcon("stretch-horizontal", Lucide.StretchHorizontal, "Stretch"),
        CuratedCalendarIcon("timer-reset", Lucide.TimerReset, "Intervals"),
    )

    fun resolve(key: String): ImageVector =
        curated.find { it.key == key }?.icon ?: Lucide.Activity
}
