package com.gainsmaxxing.ui.calendar

import android.graphics.Color as PlatformColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.gainsmaxxing.ui.theme.BorderSubtle
import com.gainsmaxxing.ui.theme.Surface2
import com.gainsmaxxing.ui.theme.TextSecondary
import com.gainsmaxxing.ui.theme.caption

@Composable
fun HsvColorPicker(
    color: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialHsv = remember(color) { colorToHsv(color) }
    var hue by remember(color) { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember(color) { mutableFloatStateOf(initialHsv[1]) }
    var value by remember(color) { mutableFloatStateOf(initialHsv[2]) }

    fun emitColor() {
        onColorChange(hsvToColor(hue, saturation, value))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(hsvToColor(hue, saturation, value))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp)),
            )
            Text(
                text = rgbLabel(hsvToColor(hue, saturation, value)),
                style = MaterialTheme.typography.caption,
                color = TextSecondary,
            )
        }

        SaturationValuePanel(
            hue = hue,
            saturation = saturation,
            value = value,
            onSaturationValueChange = { newSaturation, newValue ->
                saturation = newSaturation
                value = newValue
                emitColor()
            },
        )

        HueSlider(
            hue = hue,
            onHueChange = {
                hue = it
                emitColor()
            },
        )
    }
}

@Composable
private fun SaturationValuePanel(
    hue: Float,
    saturation: Float,
    value: Float,
    onSaturationValueChange: (Float, Float) -> Unit,
) {
    var panelWidth by remember { mutableFloatStateOf(1f) }
    var panelHeight by remember { mutableFloatStateOf(1f) }
    val hueColor = hsvToColor(hue, 1f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .onSizeChanged { size ->
                panelWidth = size.width.toFloat().coerceAtLeast(1f)
                panelHeight = size.height.toFloat().coerceAtLeast(1f)
            }
            .pointerInput(hue) {
                detectTapGestures { offset ->
                    val newSaturation = (offset.x / panelWidth).coerceIn(0f, 1f)
                    val newValue = 1f - (offset.y / panelHeight).coerceIn(0f, 1f)
                    onSaturationValueChange(newSaturation, newValue)
                }
            }
            .pointerInput(hue) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val newSaturation = (change.position.x / panelWidth).coerceIn(0f, 1f)
                    val newValue = 1f - (change.position.y / panelHeight).coerceIn(0f, 1f)
                    onSaturationValueChange(newSaturation, newValue)
                }
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(hueColor),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.White, Color.Transparent),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black),
                    ),
                ),
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val x = saturation * size.width
            val y = (1f - value) * size.height
            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx(),
                center = Offset(x, y),
                style = Stroke(width = 2.dp.toPx()),
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.35f),
                radius = 8.dp.toPx(),
                center = Offset(x, y),
                style = Stroke(width = 1.dp.toPx()),
            )
        }
    }
}

@Composable
private fun HueSlider(
    hue: Float,
    onHueChange: (Float) -> Unit,
) {
    var sliderWidth by remember { mutableFloatStateOf(1f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .onSizeChanged { size ->
                sliderWidth = size.width.toFloat().coerceAtLeast(1f)
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onHueChange((offset.x / sliderWidth * 360f).coerceIn(0f, 360f))
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    onHueChange((change.position.x / sliderWidth * 360f).coerceIn(0f, 360f))
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val colors = (0..6).map { index ->
                hsvToColor(index / 6f * 360f, 1f, 1f)
            }
            drawRect(
                brush = Brush.horizontalGradient(colors),
            )
            val thumbX = (hue / 360f) * size.width
            drawCircle(
                color = Color.White,
                radius = 10.dp.toPx(),
                center = Offset(thumbX, size.height / 2f),
                style = Stroke(width = 2.dp.toPx()),
            )
            drawCircle(
                color = Surface2,
                radius = 8.dp.toPx(),
                center = Offset(thumbX, size.height / 2f),
            )
        }
    }
}

private fun colorToHsv(color: Color): FloatArray {
    val hsv = FloatArray(3)
    PlatformColor.colorToHSV(color.toArgb(), hsv)
    return hsv
}

private fun hsvToColor(h: Float, s: Float, v: Float): Color =
    Color(PlatformColor.HSVToColor(floatArrayOf(h, s, v)))

private fun rgbLabel(color: Color): String {
    val argb = color.toArgb()
    val red = PlatformColor.red(argb)
    val green = PlatformColor.green(argb)
    val blue = PlatformColor.blue(argb)
    return "RGB $red, $green, $blue"
}
