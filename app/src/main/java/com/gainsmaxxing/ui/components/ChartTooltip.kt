package com.gainsmaxxing.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Positions a chart tooltip relative to an anchor point (a line dot or bar) inside a
 * [androidx.compose.foundation.layout.BoxWithConstraints] chart area.
 *
 * Horizontally the tooltip defaults to the left of the anchor (a [GAP] of space between its
 * right edge and [anchorX]) and flips to the right of the anchor when it would overflow the
 * left edge. The result is always clamped within `[0, chartWidth]`.
 *
 * Vertically the tooltip sits just above the anchor (the same [GAP] above [anchorTop]) and is
 * clamped so it stays within the card: it may extend above the chart area by up to
 * [topOverflow] (use this for charts that have a header above the chart giving extra headroom,
 * like the bodyweight chart) and never past the chart's bottom.
 *
 * Call from a `BoxWithConstraints` scope, passing its `maxWidth`/`maxHeight`. The [content]
 * owns the tooltip's visual chrome (background, border, padding).
 */
@Composable
fun BoxScope.ChartTooltip(
    chartWidth: Dp,
    chartHeight: Dp,
    anchorX: Dp,
    anchorTop: Dp,
    topOverflow: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .layout { measurable, _ ->
                val placeable = measurable.measure(Constraints())
                val gap = GAP.roundToPx()
                val maxW = chartWidth.roundToPx()
                val maxH = chartHeight.roundToPx()

                // Default to the left of the anchor; flip to the right if it would overflow.
                val left = anchorX.roundToPx() - gap - placeable.width
                val x = if (left >= 0) left else anchorX.roundToPx() + gap
                val xClamped = x.coerceIn(0, (maxW - placeable.width).coerceAtLeast(0))

                val y = anchorTop.roundToPx() - gap - placeable.height
                val minY = -topOverflow.roundToPx()
                val yClamped = y.coerceIn(minY, (maxH - placeable.height).coerceAtLeast(minY))

                layout(0, 0) {
                    placeable.place(xClamped, yClamped)
                }
            },
    ) {
        content()
    }
}

private val GAP = 8.dp
