package com.gainsmaxxing.ui.calendar

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.unit.dp

private fun lucidePathIcon(name: String, vararg paths: String): ImageVector =
    Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        paths.forEach { pathData ->
            addPath(
                pathData = PathParser().parsePathString(pathData).toNodes(),
                pathFillType = PathFillType.NonZero,
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineMiter = 4f,
            )
        }
    }.build()

/** Lucide `sport-shoe` — not yet in composables/icons-lucide 1.1.0. */
val LucideSportShoe: ImageVector = lucidePathIcon(
    name = "SportShoe",
    "m15 10.42 4.8-5.07",
    "M19 18h3",
    "M9.5 22 21.414 9.415A2 2 0 0 0 21.2 6.4l-5.61-4.208A1 1 0 0 0 14 3v2a2 2 0 0 1-1.394 1.906L8.677 8.053A1 1 0 0 0 8 9c-.155 6.393-2.082 9-4 9a2 2 0 0 0 0 4h14",
)

/** Lucide Lab `basketball` — not in the main icon package. */
val LucideBasketball: ImageVector = lucidePathIcon(
    name = "Basketball",
    "M 12 12 m -10 0 a 10 10 0 1 0 20 0 a 10 10 0 1 0 -20 0",
    "M2.1 13.4A10.1 10.1 0 0 0 13.4 2.1",
    "m5 4.9 14 14.2",
    "M21.9 10.6a10.1 10.1 0 0 0-11.3 11.3",
)
