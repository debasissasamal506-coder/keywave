package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0x221E233E),
    borderColor: Color = Color(0x334E5D8F),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                border = BorderStroke(1.dp, borderColor),
                shape = RoundedCornerShape(16.dp)
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun SettingSectionHeader(title: String, subtitle: String? = null) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF38BDF8)
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFFA0AEC0)
            )
        }
    }
}

@Composable
fun GlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    label: String,
    valueDisplay: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color(0xFFE2E8F0),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = valueDisplay,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF38BDF8),
                activeTrackColor = Color(0xFF818CF8),
                inactiveTrackColor = Color(0x44FFFFFF)
            )
        )
    }
}

@Composable
fun GlassSwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFF1F5F9)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF38BDF8),
                checkedTrackColor = Color(0xFF818CF8),
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0x33FFFFFF)
            )
        )
    }
}

@Composable
fun <T> SegmentedControl(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    itemLabel: (T) -> String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x33101426))
            .border(1.dp, Color(0x334E5D8F), RoundedCornerShape(12.dp))
            .padding(3.dp)
    ) {
        items.forEach { item ->
            val isSelected = item == selectedItem
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(
                        if (isSelected) Brush.horizontalGradient(
                            listOf(Color(0xFF38BDF8), Color(0xFF818CF8))
                        ) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .clickable { onItemSelected(item) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = itemLabel(item),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
fun ColorSwatchSelector(
    colors: List<Long>,
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        colors.forEach { colorVal ->
            val isSelected = colorVal == selectedColor
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(colorVal))
                    .border(
                        width = if (isSelected) 2.5.dp else 1.dp,
                        color = if (isSelected) Color.White else Color(0x55FFFFFF),
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(colorVal) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Interactive Hue/Saturation/Value Color Picker for full custom color selection (Requirement 4)
 */
@Composable
fun InteractiveColorPicker(
    label: String,
    currentColor: Long,
    onColorChanged: (Long) -> Unit,
    presetSwatches: List<Long> = listOf(
        0xFF12131A, 0xFF1C1E26, 0xFF242632, 0xFF38BDF8, 0xFF818CF8,
        0xFFC084FC, 0xFF34D399, 0xFFF472B6, 0xFFFBBF24, 0xFFF1F5F9
    )
) {
    var hue by remember(currentColor) {
        mutableFloatStateOf(colorToHue(currentColor))
    }
    var lightness by remember(currentColor) {
        mutableFloatStateOf(colorToBrightness(currentColor))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x18192644))
            .border(1.dp, Color(0x284E5D8F), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = String.format("#%06X", (0xFFFFFF and currentColor.toInt())),
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(end = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(currentColor))
                        .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Swatches
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            presetSwatches.forEach { swatch ->
                val isSelected = swatch == currentColor
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(swatch))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color.White else Color(0x44FFFFFF),
                            shape = CircleShape
                        )
                        .clickable {
                            onColorChanged(swatch)
                            hue = colorToHue(swatch)
                            lightness = colorToBrightness(swatch)
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Hue Spectrum Slider
        Text(text = "Hue Spectrum", fontSize = 11.sp, color = Color(0xFF94A3B8))
        Slider(
            value = hue,
            onValueChange = { newHue ->
                hue = newHue
                val updated = hsvToLong(newHue, 0.85f, lightness)
                onColorChanged(updated)
            },
            valueRange = 0f..360f,
            colors = SliderDefaults.colors(
                thumbColor = Color(currentColor),
                activeTrackColor = Color(0xFF38BDF8),
                inactiveTrackColor = Color(0x33FFFFFF)
            )
        )

        // Lightness Slider
        Text(text = "Lightness / Shade", fontSize = 11.sp, color = Color(0xFF94A3B8))
        Slider(
            value = lightness,
            onValueChange = { newLight ->
                lightness = newLight
                val updated = hsvToLong(hue, 0.85f, newLight)
                onColorChanged(updated)
            },
            valueRange = 0.05f..1.0f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF818CF8),
                inactiveTrackColor = Color(0x33FFFFFF)
            )
        )
    }
}

private fun colorToHue(colorLong: Long): Float {
    val r = ((colorLong shr 16) and 0xFF) / 255f
    val g = ((colorLong shr 8) and 0xFF) / 255f
    val b = (colorLong and 0xFF) / 255f
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min
    if (delta < 0.0001f) return 0f
    return when (max) {
        r -> 60f * (((g - b) / delta) % 6f)
        g -> 60f * (((b - r) / delta) + 2f)
        else -> 60f * (((r - g) / delta) + 4f)
    }.let { if (it < 0f) it + 360f else it }
}

private fun colorToBrightness(colorLong: Long): Float {
    val r = ((colorLong shr 16) and 0xFF) / 255f
    val g = ((colorLong shr 8) and 0xFF) / 255f
    val b = (colorLong and 0xFF) / 255f
    return maxOf(r, g, b).coerceIn(0.1f, 1f)
}

private fun hsvToLong(hue: Float, sat: Float, value: Float): Long {
    val h = (hue % 360f) / 60f
    val c = value * sat
    val x = c * (1f - kotlin.math.abs(h % 2f - 1f))
    val m = value - c
    val (r, g, b) = when {
        h < 1f -> Triple(c, x, 0f)
        h < 2f -> Triple(x, c, 0f)
        h < 3f -> Triple(0f, c, x)
        h < 4f -> Triple(0f, x, c)
        h < 5f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    val ri = ((r + m) * 255f).roundToInt().coerceIn(0, 255)
    val gi = ((g + m) * 255f).roundToInt().coerceIn(0, 255)
    val bi = ((b + m) * 255f).roundToInt().coerceIn(0, 255)
    return (0xFFL shl 24) or (ri.toLong() shl 16) or (gi.toLong() shl 8) or bi.toLong()
}
