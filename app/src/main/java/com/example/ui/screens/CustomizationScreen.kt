package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.KeyWaveSoundManager
import com.example.data.KeyboardPreferences
import com.example.haptics.KeyWaveHapticManager
import com.example.keyboard.KeyboardLayoutView
import com.example.model.GradientType
import com.example.model.HapticStrength
import com.example.model.KeyHeightOption
import com.example.model.KeyShape
import com.example.model.KeyboardConfig
import com.example.model.KeyboardSizeOption
import com.example.model.RGBMode
import com.example.model.SideMargins
import com.example.model.SoundStyle
import com.example.model.WaveIntensity
import com.example.model.WaveSpeed
import com.example.ui.components.ColorSwatchSelector
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassSlider
import com.example.ui.components.GlassSwitchRow
import com.example.ui.components.InteractiveColorPicker
import com.example.ui.components.SegmentedControl
import com.example.ui.components.SettingSectionHeader
import com.example.ui.components.WaveColorCustomizer

@Composable
fun CustomizationScreen(
    config: KeyboardConfig,
    preferences: KeyboardPreferences,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val hapticManager = remember { KeyWaveHapticManager(context) }
    val soundManager = remember { KeyWaveSoundManager(context) }
    DisposableEffect(soundManager) {
        onDispose {
            soundManager.release()
        }
    }
    var selectedCategory by remember { mutableIntStateOf(0) }
    val categories = listOf("Layout & Size", "Colors & Palette", "Gradients", "RGB Lighting", "Haptics & Sound", "Modes")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A10))
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Customize Keyboard",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Live Sticky Keyboard Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x18182035))
                .border(1.dp, Color(0x334E5D8F), RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE PREVIEW",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${config.rgbMode.displayName} • ${config.keyShape.displayName}",
                        fontSize = 10.sp,
                        color = Color(0xFF38BDF8)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                KeyboardLayoutView(
                    config = config,
                    onKeyPress = { key ->
                        if (config.vibrateOnKeypress) {
                            hapticManager.vibrateKeypress(config.hapticStrength)
                        }
                        if (config.soundOnKeypress) {
                            soundManager.playKeySound(config.soundStyle, config.soundVolume)
                        }
                    },
                    currentWordPrefix = "Key",
                    onUpdateConfig = { update ->
                        preferences.updateConfig { update(it) }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Categories Header Horizontal Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.take(3).forEachIndexed { index, title ->
                val isSelected = selectedCategory == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) Color(0xFF38BDF8)
                            else Color(0x18192644)
                        )
                        .clickable { selectedCategory = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF090A10) else Color(0xFF94A3B8)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.drop(3).forEachIndexed { subIndex, title ->
                val index = subIndex + 3
                val isSelected = selectedCategory == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) Color(0xFF38BDF8)
                            else Color(0x18192644)
                        )
                        .clickable { selectedCategory = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF090A10) else Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Settings Category Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (selectedCategory) {
                0 -> {
                    // Layout & Size
                    GlassCard {
                        SettingSectionHeader("Keyboard Size", "Adjust overall scale to fit screen size")
                        val sizeOptions = KeyboardSizeOption.values().toList()
                        SegmentedControl(
                            items = sizeOptions,
                            selectedItem = sizeOptions.find { it.percent == config.keyboardSizePercent } ?: KeyboardSizeOption.MEDIUM,
                            onItemSelected = {
                                preferences.updateConfig { c -> c.copy(keyboardSizePercent = it.percent) }
                            },
                            itemLabel = { it.displayName }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        GlassSlider(
                            value = config.keyboardSizePercent.toFloat(),
                            onValueChange = {
                                preferences.updateConfig { c -> c.copy(keyboardSizePercent = it.toInt()) }
                            },
                            valueRange = 70f..120f,
                            label = "Fine Scale",
                            valueDisplay = "${config.keyboardSizePercent}%"
                        )
                    }

                    GlassCard {
                        SettingSectionHeader("Key Height", "Vertical height of each key row")
                        val heightOptions = KeyHeightOption.values().toList()
                        SegmentedControl(
                            items = heightOptions,
                            selectedItem = heightOptions.find { kotlin.math.abs(it.heightDp - config.keyHeightDp) < 2f } ?: KeyHeightOption.NORMAL,
                            onItemSelected = {
                                preferences.updateConfig { c -> c.copy(keyHeightDp = it.heightDp) }
                            },
                            itemLabel = { it.displayName }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        GlassSlider(
                            value = config.keyHeightDp,
                            onValueChange = {
                                preferences.updateConfig { c -> c.copy(keyHeightDp = it) }
                            },
                            valueRange = 40f..60f,
                            label = "Exact Height",
                            valueDisplay = "${config.keyHeightDp.toInt()} dp"
                        )
                    }

                    GlassCard {
                        SettingSectionHeader("Key Shape", "Corner radius and styling")
                        SegmentedControl(
                            items = KeyShape.values().toList(),
                            selectedItem = config.keyShape,
                            onItemSelected = {
                                preferences.updateConfig { c -> c.copy(keyShape = it) }
                            },
                            itemLabel = { it.displayName }
                        )
                    }

                    GlassCard {
                        SettingSectionHeader("Number Row", "Quick top row for numbers 1-0")
                        GlassSwitchRow(
                            title = "Show Dedicated Number Row",
                            subtitle = "Displays 1-0 numbers above the top letters",
                            checked = config.showNumberRow,
                            onCheckedChange = {
                                preferences.updateConfig { c -> c.copy(showNumberRow = it) }
                            }
                        )
                    }

                    GlassCard {
                        SettingSectionHeader("Side Margins")
                        SegmentedControl(
                            items = SideMargins.values().toList(),
                            selectedItem = config.sideMargins,
                            onItemSelected = {
                                preferences.updateConfig { c -> c.copy(sideMargins = it) }
                            },
                            itemLabel = { it.displayName }
                        )
                    }
                }

                1 -> {
                    // Custom Color Picker (Requirement 4)
                    GlassCard {
                        SettingSectionHeader("Custom Color Mode", "Customize every color element of the keyboard")
                        GlassSwitchRow(
                            title = "Enable Custom Colors",
                            subtitle = "Overrides theme preset with your custom palette",
                            checked = config.useCustomColors,
                            onCheckedChange = {
                                preferences.updateConfig { c -> c.copy(useCustomColors = it) }
                            }
                        )
                    }

                    if (config.useCustomColors) {
                        InteractiveColorPicker(
                            label = "Keyboard Background",
                            currentColor = config.customBackground,
                            onColorChanged = { newColor ->
                                preferences.updateConfig { c -> c.copy(customBackground = newColor) }
                            }
                        )

                        InteractiveColorPicker(
                            label = "Key Background",
                            currentColor = config.customKeyColor,
                            onColorChanged = { newColor ->
                                preferences.updateConfig { c -> c.copy(customKeyColor = newColor) }
                            }
                        )

                        InteractiveColorPicker(
                            label = "Key Text Color",
                            currentColor = config.customTextColor,
                            onColorChanged = { newColor ->
                                preferences.updateConfig { c -> c.copy(customTextColor = newColor) }
                            }
                        )

                        InteractiveColorPicker(
                            label = "Accent / Primary",
                            currentColor = config.customAccent,
                            onColorChanged = { newColor ->
                                preferences.updateConfig { c -> c.copy(customAccent = newColor, customPrimary = newColor) }
                            }
                        )

                        InteractiveColorPicker(
                            label = "RGB Glow Color",
                            currentColor = config.customGlowColor,
                            onColorChanged = { newColor ->
                                preferences.updateConfig { c -> c.copy(customGlowColor = newColor) }
                            }
                        )
                    }
                }

                2 -> {
                    // Gradients (Requirement 5)
                    GlassCard {
                        SettingSectionHeader("Gradient Support", "2-color, 3-color, and rainbow styles")
                        val gradientTypes = listOf("2-color", "3-color", "rainbow")
                        SegmentedControl(
                            items = gradientTypes,
                            selectedItem = config.customGradient.type,
                            onItemSelected = { t ->
                                preferences.updateConfig { c ->
                                    c.copy(customGradient = c.customGradient.copy(type = t))
                                }
                            },
                            itemLabel = {
                                when (it) {
                                    "2-color" -> "2-Color"
                                    "3-color" -> "3-Color"
                                    else -> "Rainbow"
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        GlassSlider(
                            value = config.customGradient.directionAngle,
                            onValueChange = { angle ->
                                preferences.updateConfig { c ->
                                    c.copy(customGradient = c.customGradient.copy(directionAngle = angle))
                                }
                            },
                            valueRange = 0f..360f,
                            label = "Gradient Angle",
                            valueDisplay = "${config.customGradient.directionAngle.toInt()}°"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        GlassSlider(
                            value = config.customGradient.speed,
                            onValueChange = { spd ->
                                preferences.updateConfig { c ->
                                    c.copy(customGradient = c.customGradient.copy(speed = spd))
                                }
                            },
                            valueRange = 0.5f..2.5f,
                            label = "Flow Speed",
                            valueDisplay = "${String.format("%.1f", config.customGradient.speed)}x"
                        )
                    }

                    if (config.customGradient.type == "2-color" || config.customGradient.type == "3-color") {
                        InteractiveColorPicker(
                            label = "Gradient Color 1",
                            currentColor = config.customGradient.color1,
                            onColorChanged = { c1 ->
                                preferences.updateConfig { c ->
                                    c.copy(customGradient = c.customGradient.copy(color1 = c1))
                                }
                            }
                        )

                        InteractiveColorPicker(
                            label = "Gradient Color 2",
                            currentColor = config.customGradient.color2,
                            onColorChanged = { c2 ->
                                preferences.updateConfig { c ->
                                    c.copy(customGradient = c.customGradient.copy(color2 = c2))
                                }
                            }
                        )
                    }

                    if (config.customGradient.type == "3-color") {
                        InteractiveColorPicker(
                            label = "Gradient Color 3",
                            currentColor = config.customGradient.color3,
                            onColorChanged = { c3 ->
                                preferences.updateConfig { c ->
                                    c.copy(customGradient = c.customGradient.copy(color3 = c3))
                                }
                            }
                        )
                    }
                }

                3 -> {
                    // RGB & Wave (Requirement 6)
                    GlassCard {
                        SettingSectionHeader("RGB Effect Mode", "Static, reactive wave, pulse, breathing, rainbow")
                        val modes = RGBMode.values().toList()
                        modes.chunked(3).forEach { rowModes ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowModes.forEach { mode ->
                                    val isSelected = config.rgbMode == mode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) Color(0x3338BDF8) else Color(0x181F2643))
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) Color(0xFF38BDF8) else Color(0x224E5D8F),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable {
                                                preferences.updateConfig { c -> c.copy(rgbMode = mode) }
                                            }
                                            .padding(vertical = 10.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = mode.displayName,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    GlassCard {
                        SettingSectionHeader("Wave Settings", "Speed and intensity of concentric wave ripples")
                        Text(
                            text = "Wave Speed",
                            fontSize = 13.sp,
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        SegmentedControl(
                            items = WaveSpeed.values().toList(),
                            selectedItem = config.waveSpeed,
                            onItemSelected = {
                                preferences.updateConfig { c -> c.copy(waveSpeed = it) }
                            },
                            itemLabel = { it.displayName }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Wave Intensity",
                            fontSize = 13.sp,
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        SegmentedControl(
                            items = WaveIntensity.values().toList(),
                            selectedItem = config.waveIntensity,
                            onItemSelected = {
                                preferences.updateConfig { c -> c.copy(waveIntensity = it) }
                            },
                            itemLabel = { it.displayName }
                        )
                    }

                    GlassCard {
                        SettingSectionHeader("Brightness & Animation")
                        GlassSlider(
                            value = config.rgbBrightness,
                            onValueChange = {
                                preferences.updateConfig { c -> c.copy(rgbBrightness = it) }
                            },
                            valueRange = 0.0f..1.0f,
                            label = "RGB Brightness",
                            valueDisplay = "${(config.rgbBrightness * 100).toInt()}%"
                        )
                    }
                }

                4 -> {
                    // Haptics & Sound (Requirement 8)
                    GlassCard {
                        SettingSectionHeader("Haptic Vibration Feedback")
                        GlassSwitchRow(
                            title = "Vibrate on Keypress",
                            checked = config.vibrateOnKeypress,
                            onCheckedChange = {
                                preferences.updateConfig { c -> c.copy(vibrateOnKeypress = it) }
                            }
                        )

                        if (config.vibrateOnKeypress) {
                            Spacer(modifier = Modifier.height(8.dp))
                            SegmentedControl(
                                items = HapticStrength.values().toList(),
                                selectedItem = config.hapticStrength,
                                onItemSelected = { strength ->
                                    preferences.updateConfig { c -> c.copy(hapticStrength = strength) }
                                    hapticManager.vibrateKeypress(strength)
                                },
                                itemLabel = { it.displayName }
                            )
                        }
                    }

                    GlassCard {
                        SettingSectionHeader("Key Sounds", "Audio feedback on each key strike")
                        GlassSwitchRow(
                            title = "Sound on Keypress",
                            checked = config.soundOnKeypress,
                            onCheckedChange = {
                                preferences.updateConfig { c -> c.copy(soundOnKeypress = it) }
                            }
                        )

                        if (config.soundOnKeypress) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val styles = SoundStyle.values().toList()
                            styles.chunked(3).forEach { rowStyles ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowStyles.forEach { style ->
                                        val isSelected = config.soundStyle == style
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) Color(0x3338BDF8) else Color(0x181F2643))
                                                .border(
                                                    width = if (isSelected) 1.5.dp else 1.dp,
                                                    color = if (isSelected) Color(0xFF38BDF8) else Color(0x224E5D8F),
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .clickable {
                                                    preferences.updateConfig { c -> c.copy(soundStyle = style) }
                                                    soundManager.playKeySound(style, config.soundVolume)
                                                }
                                                .padding(vertical = 10.dp, horizontal = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = style.displayName,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            GlassSlider(
                                value = config.soundVolume,
                                onValueChange = {
                                    preferences.updateConfig { c -> c.copy(soundVolume = it) }
                                },
                                valueRange = 0.0f..1.0f,
                                label = "Sound Volume",
                                valueDisplay = "${(config.soundVolume * 100).toInt()}%"
                            )
                        }
                    }
                }

                5 -> {
                    // Modes
                    GlassCard {
                        SettingSectionHeader("Minimal Modern Style", "Clean dark minimal aesthetic with zero clutter")
                        GlassSwitchRow(
                            title = "Minimalistic Mode",
                            subtitle = "Disables intense lighting for understated elegance",
                            checked = config.minimalisticMode,
                            onCheckedChange = {
                                preferences.updateConfig { c -> c.copy(minimalisticMode = it) }
                            }
                        )
                    }

                    GlassCard {
                        SettingSectionHeader("Premium Glass Style", "Semi-transparent frosted glass aesthetic")
                        GlassSwitchRow(
                            title = "Frosted Glass Mode",
                            subtitle = "Translucent key surfaces with soft border highlights",
                            checked = config.glassMode,
                            onCheckedChange = {
                                preferences.updateConfig { c -> c.copy(glassMode = it) }
                            }
                        )
                    }

                    GlassCard {
                        SettingSectionHeader("Word Suggestions & Auto-Correction", "100% offline privacy")
                        GlassSwitchRow(
                            title = "Suggestion Bar",
                            subtitle = "Displays next word suggestions above the keyboard",
                            checked = config.showSuggestions,
                            onCheckedChange = {
                                preferences.updateConfig { c -> c.copy(showSuggestions = it) }
                            }
                        )
                        GlassSwitchRow(
                            title = "Auto-Correction",
                            subtitle = "Automatically corrects common typos on space",
                            checked = config.autoCorrection,
                            onCheckedChange = {
                                preferences.updateConfig { c -> c.copy(autoCorrection = it) }
                            }
                        )
                    }

                    GlassCard {
                        SettingSectionHeader("Performance Mode", "Optimized for speed and lower battery usage")
                        GlassSwitchRow(
                            title = "Low-Latency Performance",
                            subtitle = "Prioritizes immediate touch-to-type response",
                            checked = config.performanceMode,
                            onCheckedChange = {
                                preferences.updateConfig { c -> c.copy(performanceMode = it) }
                            }
                        )
                        GlassSwitchRow(
                            title = "Battery Saver",
                            subtitle = "Dims lighting and reduces background animations",
                            checked = config.batterySaverMode,
                            onCheckedChange = {
                                preferences.updateConfig { c -> c.copy(batterySaverMode = it) }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
