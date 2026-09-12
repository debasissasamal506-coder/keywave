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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardPreferences
import com.example.model.KeyboardConfig
import com.example.model.KeyboardTheme
import com.example.model.ThemePresets
import com.example.ui.components.GlassCard

@Composable
fun ThemesScreen(
    config: KeyboardConfig,
    preferences: KeyboardPreferences,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070814))
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
                text = "Theme Gallery",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(ThemePresets.ALL_THEMES) { theme ->
                ThemeCard(
                    theme = theme,
                    isSelected = config.themeId == theme.id && !config.useCustomColors,
                    onSelect = {
                        preferences.updateConfig {
                            it.copy(
                                themeId = theme.id,
                                useCustomColors = false,
                                rgbMode = theme.defaultRgbMode
                            )
                        }
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ThemeCard(
    theme: KeyboardTheme,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    GlassCard(
        modifier = Modifier.clickable { onSelect() },
        backgroundColor = Color(theme.backgroundColor).copy(alpha = 0.85f),
        borderColor = if (isSelected) Color(theme.accentColor) else Color(theme.borderColor).copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = theme.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(theme.textColor)
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(theme.accentColor))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF090B14)
                            )
                        }
                    }
                }
                Text(
                    text = "Mode: ${theme.defaultRgbMode.displayName}",
                    fontSize = 12.sp,
                    color = Color(theme.specialTextColor)
                )
            }

            // Theme Swatch Palette Preview
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    theme.keyColor,
                    theme.accentColor,
                    theme.secondaryColor,
                    theme.glowColor
                ).forEach { colorVal ->
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(colorVal))
                            .border(1.dp, Color(0x33FFFFFF), CircleShape)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Mini keyboard key preview row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val previewKeys = listOf("Q", "W", "E", "R", "T", "Y")
            previewKeys.forEach { keyLabel ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(theme.keyColor))
                        .border(1.dp, Color(theme.borderColor).copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = keyLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(theme.textColor)
                    )
                }
            }
        }
    }
}
