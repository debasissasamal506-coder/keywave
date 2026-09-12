package com.example.keyboard.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeyboardConfig
import com.example.model.KeyboardTheme
import com.example.model.SideMargins
import kotlin.math.roundToInt

@Composable
fun KeyboardResizeDialog(
    config: KeyboardConfig,
    theme: KeyboardTheme,
    maxHeight: Dp,
    onUpdateConfig: ((KeyboardConfig) -> KeyboardConfig) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = config.scaleFactor

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(maxHeight)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (config.glassMode) Color(theme.backgroundColor).copy(alpha = 0.90f)
                else Color(theme.backgroundColor)
            )
            .border(1.dp, Color(theme.borderColor).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(10.dp)
            .testTag("keyboard_resize_panel")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Keyboard Dimensions & Resize",
                    fontSize = (13 * scale).coerceIn(12f, 15f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(theme.textColor)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Reset Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(theme.keyColor).copy(alpha = 0.6f))
                        .clickable {
                            onUpdateConfig {
                                it.copy(
                                    keyboardWidthPercent = 100,
                                    keyboardSizePercent = 100,
                                    keyHeightDp = 48f,
                                    keySpacingDp = 4f,
                                    sideMargins = SideMargins.NORMAL
                                )
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset",
                            tint = Color(theme.specialTextColor),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Reset",
                            fontSize = 10.sp,
                            color = Color(theme.specialTextColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(theme.keyColor).copy(alpha = 0.6f))
                        .clickable { onClose() }
                        .padding(5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Done",
                        tint = Color(theme.textColor).copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Sliders
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Width Slider
            ResizeSliderRow(
                label = "Keyboard Width",
                valueDisplay = "${config.keyboardWidthPercent}%",
                value = config.keyboardWidthPercent.toFloat(),
                range = 75f..100f,
                steps = 25,
                theme = theme,
                onValueChange = { newVal ->
                    onUpdateConfig { it.copy(keyboardWidthPercent = newVal.roundToInt()) }
                }
            )

            // Height Slider
            ResizeSliderRow(
                label = "Key Height",
                valueDisplay = "${config.keyHeightDp.roundToInt()} dp",
                value = config.keyHeightDp,
                range = 40f..60f,
                steps = 20,
                theme = theme,
                onValueChange = { newVal ->
                    onUpdateConfig { it.copy(keyHeightDp = newVal) }
                }
            )

            // Scale / Font Size Slider
            ResizeSliderRow(
                label = "Keyboard Scale",
                valueDisplay = "${config.keyboardSizePercent}%",
                value = config.keyboardSizePercent.toFloat(),
                range = 75f..120f,
                steps = 9,
                theme = theme,
                onValueChange = { newVal ->
                    onUpdateConfig { it.copy(keyboardSizePercent = newVal.roundToInt()) }
                }
            )

            // Key Gap / Spacing Slider
            ResizeSliderRow(
                label = "Key Spacing",
                valueDisplay = "${config.keySpacingDp.roundToInt()} dp",
                value = config.keySpacingDp,
                range = 2f..6f,
                steps = 4,
                theme = theme,
                onValueChange = { newVal ->
                    onUpdateConfig { it.copy(keySpacingDp = newVal) }
                }
            )
        }
    }
}

@Composable
private fun ResizeSliderRow(
    label: String,
    valueDisplay: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    theme: KeyboardTheme,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.width(110.dp)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(theme.textColor)
            )
            Text(
                text = valueDisplay,
                fontSize = 10.sp,
                color = Color(theme.accentColor),
                fontWeight = FontWeight.Bold
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            steps = steps,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = Color(theme.accentColor),
                activeTrackColor = Color(theme.accentColor),
                inactiveTrackColor = Color(theme.borderColor).copy(alpha = 0.3f)
            )
        )
    }
}
