package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeyShape
import com.example.model.KeyboardConfig
import com.example.model.WaveColorMode
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sqrt

// Preset colors for instant one-tap selection (Requirement 6)
data class WaveColorPreset(val name: String, val colorValue: Long)

val POPULAR_WAVE_COLORS = listOf(
    WaveColorPreset("Red", 0xFFFF2255),
    WaveColorPreset("Blue", 0xFF2563EB),
    WaveColorPreset("Green", 0xFF10B981),
    WaveColorPreset("Cyan", 0xFF00F5D4),
    WaveColorPreset("Purple", 0xFF8B5CF6),
    WaveColorPreset("Pink", 0xFFFF2D85),
    WaveColorPreset("Orange", 0xFFFF6B00),
    WaveColorPreset("Yellow", 0xFFFBBF24),
    WaveColorPreset("Emerald", 0xFF059669),
    WaveColorPreset("White", 0xFFFFFFFF)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WaveColorCustomizer(
    config: KeyboardConfig,
    onUpdateConfig: ((KeyboardConfig) -> KeyboardConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Waves,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Global Wave Color & Press Effect",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Single global color ripples when ANY key is pressed",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        // Live Interactive Wave Preview Card (Requirement 20)
        LiveWavePreviewCard(config = config)

        // 1. Wave Mode Switcher: Solid Color vs. RGB / Rainbow (Requirement 6)
        Text(
            text = "Wave Color Style",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFCBD5E1)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x33101426))
                .border(1.dp, Color(0x334E5D8F), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val isSolid = config.waveColorMode == WaveColorMode.SOLID
            // Solid Color Option
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(
                        if (isSolid) Brush.horizontalGradient(
                            listOf(Color(0xFF38BDF8), Color(0xFF818CF8))
                        ) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .clickable {
                        onUpdateConfig { it.copy(waveColorMode = WaveColorMode.SOLID) }
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(config.globalWaveColor))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Solid Color",
                        fontSize = 13.sp,
                        fontWeight = if (isSolid) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSolid) Color.White else Color(0xFF94A3B8)
                    )
                }
            }

            // RGB / Rainbow Option
            val isRainbow = config.waveColorMode == WaveColorMode.RAINBOW
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(
                        if (isRainbow) Brush.horizontalGradient(
                            listOf(Color(0xFFFF0055), Color(0xFF7928CA), Color(0xFF00F5D4))
                        ) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .clickable {
                        onUpdateConfig { it.copy(waveColorMode = WaveColorMode.RAINBOW) }
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🌈 RGB / Rainbow",
                        fontSize = 13.sp,
                        fontWeight = if (isRainbow) FontWeight.Bold else FontWeight.Medium,
                        color = if (isRainbow) Color.White else Color(0xFF94A3B8)
                    )
                }
            }
        }

        // When in Solid Color mode: Show Swatches, Custom Color Picker & HEX Input (Requirement 6)
        if (config.waveColorMode == WaveColorMode.SOLID) {
            // Preset Color Swatches
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select Global Wave Color",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFA0AEC0)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    POPULAR_WAVE_COLORS.forEach { preset ->
                        val isSelected = (config.globalWaveColor and 0xFFFFFF) == (preset.colorValue and 0xFFFFFF)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) Color(preset.colorValue).copy(alpha = 0.25f)
                                    else Color(0x221E233E)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(preset.colorValue) else Color(0x33FFFFFF),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    onUpdateConfig { it.copy(globalWaveColor = preset.colorValue) }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(preset.colorValue))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (preset.name == "White" || preset.name == "Yellow") Color.Black else Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = preset.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }

            // Interactive Color Picker (Hue + Lightness) (Requirement 6)
            InteractiveHueSaturationPicker(
                currentColor = config.globalWaveColor,
                onColorChanged = { newColor ->
                    onUpdateConfig { it.copy(globalWaveColor = newColor) }
                }
            )

            // HEX Color Input (Requirement 6)
            HexColorInputField(
                currentColor = config.globalWaveColor,
                onColorChanged = { newColor ->
                    onUpdateConfig { it.copy(globalWaveColor = newColor) }
                }
            )
        } else {
            // Rainbow mode info card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0x33FF0055),
                                Color(0x337928CA),
                                Color(0x3300F5D4)
                            )
                        )
                    )
                    .border(1.dp, Color(0x4400F5D4), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "✨ RGB Rainbow Wave Active",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // 2. Wave Opacity Control (Requirement 7)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0x18192644))
                .border(1.dp, Color(0x284E5D8F), RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Opacity,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Wave Opacity",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
                val pct = (config.waveOpacity * 100).roundToInt()
                Text(
                    text = if (pct == 0) "0% (Invisible)" else "$pct%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (pct == 0) Color(0xFFEF4444) else Color(0xFF38BDF8)
                )
            }

            // Quick Opacity Buttons: 0%, 25%, 50%, 75%, 100% (Requirement 7)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    0.00f to "0%",
                    0.25f to "25%",
                    0.50f to "50%",
                    0.75f to "75%",
                    1.00f to "100%"
                ).forEach { (presetVal, label) ->
                    val isSelected = kotlin.math.abs(config.waveOpacity - presetVal) < 0.04f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) Color(0xFF38BDF8)
                                else Color(0x28233054)
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color.White else Color(0x334E5D8F),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onUpdateConfig { it.copy(waveOpacity = presetVal) }
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF090A10) else Color(0xFFCBD5E1)
                        )
                    }
                }
            }

            // Smooth Continuous Slider (Requirement 7)
            Slider(
                value = config.waveOpacity,
                onValueChange = { newOpacity ->
                    onUpdateConfig { it.copy(waveOpacity = newOpacity) }
                },
                valueRange = 0.0f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF38BDF8),
                    activeTrackColor = Color(0xFF38BDF8),
                    inactiveTrackColor = Color(0x33FFFFFF)
                )
            )
        }
    }
}

/**
 * Interactive Live Preview Component for Settings (Requirement 20)
 * Allows tapping sample keys to immediately verify the global wave color & opacity!
 */
@Composable
fun LiveWavePreviewCard(
    config: KeyboardConfig,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val previewWaves = remember { mutableStateListOf<PreviewWave>() }
    var previewTick by remember { mutableStateOf(0L) }

    LaunchedEffect(previewWaves.size) {
        if (previewWaves.isNotEmpty()) {
            while (previewWaves.isNotEmpty()) {
                withFrameMillis { now ->
                    previewTick = now
                    previewWaves.removeAll { now - it.timestamp > 800L }
                }
            }
            previewTick = 0L
        }
    }

    // Key shape radius according to config (Requirement 11 & 12)
    val keyShape = remember(config.keyShape) {
        when (config.keyShape) {
            KeyShape.ROUNDED -> RoundedCornerShape(10.dp)
            KeyShape.SOFT_ROUNDED -> RoundedCornerShape(6.dp)
            KeyShape.PILL -> RoundedCornerShape(999.dp)
            KeyShape.MINIMAL -> RoundedCornerShape(2.dp)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF10121C))
            .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LIVE TAP PREVIEW",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "Tap keys to test wave effect",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            // Sample Keys Row 1
            val sampleRow1 = listOf("Q" to 0, "W" to 1, "E" to 2, "R" to 3, "T" to 4, "Y" to 5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                sampleRow1.forEach { (char, col) ->
                    PreviewKeyCell(
                        label = char,
                        row = 0,
                        col = col,
                        config = config,
                        keyShape = keyShape,
                        waves = previewWaves,
                        tick = previewTick,
                        onTap = {
                            val now = System.currentTimeMillis()
                            previewWaves.removeAll { now - it.timestamp > 800L }
                            previewWaves.add(PreviewWave(0, col, now))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Sample Spacebar Row (Pill shape test)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PreviewKeyCell(
                    label = "123",
                    row = 1,
                    col = 0,
                    config = config,
                    keyShape = keyShape,
                    waves = previewWaves,
                    tick = previewTick,
                    onTap = {
                        val now = System.currentTimeMillis()
                        previewWaves.add(PreviewWave(1, 0, now))
                    },
                    modifier = Modifier.weight(1.3f)
                )

                PreviewKeyCell(
                    label = "KeyWave Spacebar",
                    row = 1,
                    col = 2,
                    config = config,
                    keyShape = keyShape,
                    waves = previewWaves,
                    tick = previewTick,
                    onTap = {
                        val now = System.currentTimeMillis()
                        previewWaves.add(PreviewWave(1, 2, now))
                    },
                    modifier = Modifier.weight(3.5f)
                )

                PreviewKeyCell(
                    label = "Enter ⏎",
                    row = 1,
                    col = 5,
                    config = config,
                    keyShape = keyShape,
                    waves = previewWaves,
                    tick = previewTick,
                    onTap = {
                        val now = System.currentTimeMillis()
                        previewWaves.add(PreviewWave(1, 5, now))
                    },
                    modifier = Modifier.weight(1.5f)
                )
            }
        }
    }
}

data class PreviewWave(val row: Int, val col: Int, val timestamp: Long)

@Composable
private fun PreviewKeyCell(
    label: String,
    row: Int,
    col: Int,
    config: KeyboardConfig,
    keyShape: androidx.compose.ui.graphics.Shape,
    waves: List<PreviewWave>,
    tick: Long,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val pressScale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    // Calculate wave alpha across keys
    var maxWave = 0f
    if (waves.isNotEmpty() && tick > 0L) {
        val now = tick
        for (w in waves) {
            val elapsed = (now - w.timestamp).toFloat()
            if (elapsed in 0f..800f) {
                val dRow = (row - w.row).toFloat()
                val dCol = (col - w.col).toFloat()
                val dist = sqrt(dRow * dRow + dCol * dCol)

                if (dist < 0.6f) {
                    val fade = (1f - (elapsed / 400f)).coerceIn(0f, 1f)
                    val hit = fade * fade
                    if (hit > maxWave) maxWave = hit
                } else {
                    val waveDist = elapsed * 0.011f
                    val delta = kotlin.math.abs(dist - waveDist)
                    if (delta < 1.6f) {
                        val crest = 1f - (delta / 1.6f)
                        val decay = (1f - (elapsed / 800f)).coerceIn(0f, 1f)
                        val hit = crest * decay
                        if (hit > maxWave) maxWave = hit
                    }
                }
            }
        }
    }

    val effectiveIntensity = if (isPressed) maxOf(maxWave, 0.95f) else maxWave
    val waveAlpha = (effectiveIntensity * config.waveOpacity).coerceIn(0f, 1f)

    // Global Wave Color (Requirement 3, 4, 18)
    val waveColor = remember(config.waveColorMode, config.globalWaveColor, row, col, tick) {
        when (config.waveColorMode) {
            WaveColorMode.RAINBOW -> {
                val hue = ((tick / 15f) + (col * 35f) + (row * 50f)) % 360f
                hsvToComposeColor(hue, 0.90f, 1.0f)
            }
            WaveColorMode.SOLID -> Color(config.globalWaveColor)
        }
    }

    Box(
        modifier = modifier
            .height(44.dp)
            .scale(pressScale.value)
            .clip(keyShape) // Strict clipping to key shape (Requirement 11 & 12)
            .background(Color(0xFF1E202B))
            .border(
                width = if (waveAlpha > 0.08f) 1.dp else 0.5.dp,
                color = if (waveAlpha > 0.08f) waveColor.copy(alpha = (waveAlpha * 1.2f).coerceIn(0.3f, 1f))
                else Color(0x334E5D8F),
                shape = keyShape
            )
            .drawBehind {
                if (waveAlpha > 0.005f) {
                    // Smooth radial wave overlay
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                waveColor.copy(alpha = waveAlpha * 0.90f),
                                waveColor.copy(alpha = waveAlpha * 0.40f),
                                Color.Transparent
                            ),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = maxOf(size.width, size.height) * 1.25f
                        )
                    )
                }
            }
            .clickable {
                onTap()
                coroutineScope.launch {
                    isPressed = true
                    pressScale.animateTo(0.92f, tween(30))
                    pressScale.animateTo(1.0f, tween(70))
                    isPressed = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = if (label.length > 3) 10.sp else 14.sp,
            fontWeight = if (isPressed) FontWeight.Bold else FontWeight.Medium,
            color = if (isPressed) Color.White else Color(0xFFF1F5F9)
        )
    }
}

/**
 * Interactive Hue/Saturation/Lightness Custom Color Picker (Requirement 6)
 */
@Composable
fun InteractiveHueSaturationPicker(
    currentColor: Long,
    onColorChanged: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var hue by remember(currentColor) { mutableFloatStateOf(colorToHueVal(currentColor)) }
    var lightness by remember(currentColor) { mutableFloatStateOf(colorToLightnessVal(currentColor)) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x18192644))
            .border(1.dp, Color(0x284E5D8F), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ColorLens,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Custom Color Picker",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(currentColor))
                    .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
            )
        }

        // Hue Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Hue Spectrum", fontSize = 11.sp, color = Color(0xFF94A3B8))
            Text(text = "${hue.toInt()}°", fontSize = 11.sp, color = Color(0xFF38BDF8))
        }
        Slider(
            value = hue,
            onValueChange = { newHue ->
                hue = newHue
                val updated = hsvToLongVal(newHue, 0.85f, lightness)
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Shade / Brightness", fontSize = 11.sp, color = Color(0xFF94A3B8))
            Text(text = "${(lightness * 100).toInt()}%", fontSize = 11.sp, color = Color(0xFF38BDF8))
        }
        Slider(
            value = lightness,
            onValueChange = { newLight ->
                lightness = newLight
                val updated = hsvToLongVal(hue, 0.85f, newLight)
                onColorChanged(updated)
            },
            valueRange = 0.20f..1.0f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF818CF8),
                inactiveTrackColor = Color(0x33FFFFFF)
            )
        )
    }
}

/**
 * HEX Color Input Field (Requirement 6)
 */
@Composable
fun HexColorInputField(
    currentColor: Long,
    onColorChanged: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentHex = remember(currentColor) {
        String.format("%06X", 0xFFFFFF and currentColor.toInt())
    }
    var hexText by remember(currentColor) { mutableStateOf(currentHex) }
    var isError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x18192644))
            .border(1.dp, Color(0x284E5D8F), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(18.dp)
        )

        Text(
            text = "HEX #",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF38BDF8)
        )

        OutlinedTextField(
            value = hexText,
            onValueChange = { input ->
                val clean = input.filter { it.isLetterOrDigit() }.take(6).uppercase()
                hexText = clean
                if (clean.length == 6) {
                    try {
                        val parsed = clean.toLong(16)
                        val fullColor = (0xFFL shl 24) or parsed
                        onColorChanged(fullColor)
                        isError = false
                    } catch (_: Exception) {
                        isError = true
                    }
                } else {
                    isError = clean.isNotEmpty() && clean.length < 6
                }
            },
            modifier = Modifier.weight(1f),
            singleLine = true,
            isError = isError,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color(0xFFE2E8F0),
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0x334E5D8F),
                errorBorderColor = Color(0xFFEF4444)
            )
        )

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(currentColor))
                .border(1.5.dp, Color.White, CircleShape)
        )
    }
}

// Color conversion helpers
private fun colorToHueVal(colorLong: Long): Float {
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

private fun colorToLightnessVal(colorLong: Long): Float {
    val r = ((colorLong shr 16) and 0xFF) / 255f
    val g = ((colorLong shr 8) and 0xFF) / 255f
    val b = (colorLong and 0xFF) / 255f
    return maxOf(r, g, b).coerceIn(0.2f, 1f)
}

private fun hsvToLongVal(hue: Float, sat: Float, value: Float): Long {
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

private fun hsvToComposeColor(hue: Float, saturation: Float, value: Float): Color {
    val h = (hue % 360f) / 60f
    val c = value * saturation
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
    return Color(r + m, g + m, b + m, 1.0f)
}
