package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardPreferences
import com.example.model.KeyboardConfig
import com.example.model.ThemePresets
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassSwitchRow
import com.example.ui.components.SettingSectionHeader

@Composable
fun SettingsScreen(
    config: KeyboardConfig,
    preferences: KeyboardPreferences,
    onNavigateToCustomize: () -> Unit,
    onNavigateToThemes: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

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
                text = "KeyWave Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Appearance Category
            GlassCard {
                SettingSectionHeader("Appearance & Styling")
                SettingsRowItem(
                    icon = Icons.Default.Palette,
                    title = "Theme Presets",
                    subtitle = ThemePresets.getThemeById(config.themeId).name,
                    onClick = onNavigateToThemes
                )
                SettingsRowItem(
                    icon = Icons.Default.ColorLens,
                    title = "Custom Colors & Gradients",
                    subtitle = if (config.useCustomColors) "Active (Custom Palette)" else "Preset Themes",
                    onClick = onNavigateToCustomize
                )
                SettingsRowItem(
                    icon = Icons.Default.Tune,
                    title = "RGB Lighting & Wave Effects",
                    subtitle = "${config.rgbMode.displayName} • ${(config.rgbBrightness * 100).toInt()}% Brightness",
                    onClick = onNavigateToCustomize
                )
                SettingsRowItem(
                    icon = Icons.Default.Keyboard,
                    title = "Size, Height & Key Shape",
                    subtitle = "${config.keyboardSizePercent}% Size • ${config.keyShape.displayName}",
                    onClick = onNavigateToCustomize
                )
            }

            // Typing & Prediction Category
            GlassCard {
                SettingSectionHeader("Typing Experience & Suggestions")
                GlassSwitchRow(
                    title = "Suggestion Bar",
                    subtitle = "Shows predictive words while typing",
                    checked = config.showSuggestions,
                    onCheckedChange = {
                        preferences.updateConfig { c -> c.copy(showSuggestions = it) }
                    }
                )
                GlassSwitchRow(
                    title = "Auto-Correction",
                    subtitle = "Fixes misspelled words upon pressing space",
                    checked = config.autoCorrection,
                    onCheckedChange = {
                        preferences.updateConfig { c -> c.copy(autoCorrection = it) }
                    }
                )
                GlassSwitchRow(
                    title = "Number Row",
                    subtitle = "Always show dedicated number row on top",
                    checked = config.showNumberRow,
                    onCheckedChange = {
                        preferences.updateConfig { c -> c.copy(showNumberRow = it) }
                    }
                )
                GlassSwitchRow(
                    title = "Auto Capitalization",
                    subtitle = "Capitalize first word of each sentence",
                    checked = config.autoCapitalize,
                    onCheckedChange = {
                        preferences.updateConfig { c -> c.copy(autoCapitalize = it) }
                    }
                )
            }

            // Haptic & Sound Category
            GlassCard {
                SettingSectionHeader("Haptics & Audio Feedback")
                SettingsRowItem(
                    icon = Icons.Default.Vibration,
                    title = "Haptic Vibration",
                    subtitle = if (config.vibrateOnKeypress) config.hapticStrength.displayName else "Off",
                    onClick = onNavigateToCustomize
                )
                SettingsRowItem(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    title = "Key Sounds",
                    subtitle = if (config.soundOnKeypress) "${config.soundStyle.displayName} (${(config.soundVolume * 100).toInt()}%)" else "Off",
                    onClick = onNavigateToCustomize
                )
            }

            // Performance Category
            GlassCard {
                SettingSectionHeader("Performance & Battery")
                GlassSwitchRow(
                    title = "Performance Mode",
                    subtitle = "Prioritizes minimum touch-to-type latency on lower-end devices",
                    checked = config.performanceMode,
                    onCheckedChange = {
                        preferences.updateConfig { c -> c.copy(performanceMode = it) }
                    }
                )
                GlassSwitchRow(
                    title = "Battery Saver Mode",
                    subtitle = "Reduces lighting effects and ambient render load",
                    checked = config.batterySaverMode,
                    onCheckedChange = {
                        preferences.updateConfig { c -> c.copy(batterySaverMode = it) }
                    }
                )
            }

            // Privacy Category (Requirement 14)
            GlassCard {
                SettingSectionHeader("Privacy Guarantee", "Your keystrokes stay 100% on your device")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "KeyWave NEVER logs, transmits, or uploads passwords, credit cards, messages, or personal conversations. All suggestions and features run completely offline with zero telemetry tracking.",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    )
                }
            }

            // System Integration
            GlassCard {
                SettingSectionHeader("System Keyboard Setup")
                SettingsRowItem(
                    icon = Icons.Default.Keyboard,
                    title = "Manage Keyboards in System Settings",
                    subtitle = "Enable or set KeyWave as your default Android IME",
                    onClick = {
                        try {
                            context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                        } catch (_: Exception) {
                            Toast.makeText(context, "Open Android Settings -> System -> Languages & Input", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }

            // About Category
            GlassCard {
                SettingSectionHeader("About")
                SettingsRowItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    subtitle = "KeyWave 2.0 (Modern Android IME)",
                    onClick = {}
                )
                SettingsRowItem(
                    icon = Icons.Default.Star,
                    title = "Rate KeyWave",
                    subtitle = "Share your feedback and typing experience",
                    onClick = {
                        Toast.makeText(context, "Thank you for supporting KeyWave!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color(0xFF38BDF8),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color(0xFF64748B),
            modifier = Modifier.size(16.dp)
        )
    }
}
