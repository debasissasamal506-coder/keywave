package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardPreferences
import com.example.keyboard.KeyboardLayoutView
import com.example.model.KeyboardConfig
import com.example.model.ThemePresets
import com.example.ui.components.GlassCard

@Composable
fun OnboardingScreen(
    config: KeyboardConfig,
    preferences: KeyboardPreferences,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(false) }
    var isSelected by remember { mutableStateOf(false) }
    var testText by remember { mutableStateOf("") }

    fun checkImeStatus() {
        try {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            val enabledList = imm?.enabledInputMethodList ?: emptyList()
            isEnabled = enabledList.any { it.packageName == context.packageName }

            val currentIme = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.DEFAULT_INPUT_METHOD
            )
            isSelected = currentIme?.contains(context.packageName) == true
        } catch (_: Exception) {}
    }

    LaunchedEffect(Unit) {
        checkImeStatus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080914))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Hero Title
        Text(
            text = "Welcome to KeyWave",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Type with Style • Smooth RGB • Ultra Responsive",
            fontSize = 13.sp,
            color = Color(0xFF00F5D4),
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // Step 1: Enable Keyboard
        OnboardingStepCard(
            stepNumber = 1,
            title = "Enable KeyWave Keyboard",
            description = "Activate KeyWave in Android Language & Input settings",
            isCompleted = isEnabled,
            icon = Icons.Default.Settings,
            actionLabel = if (isEnabled) "Enabled ✓" else "Open Settings",
            onAction = {
                try {
                    context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                } catch (_: Exception) {}
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Step 2: Select KeyWave
        OnboardingStepCard(
            stepNumber = 2,
            title = "Select KeyWave as Default",
            description = "Switch your active keyboard to KeyWave",
            isCompleted = isSelected,
            icon = Icons.Default.Keyboard,
            actionLabel = if (isSelected) "Active ✓" else "Choose Keyboard",
            onAction = {
                try {
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.showInputMethodPicker()
                } catch (_: Exception) {}
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Step 3: Choose First Theme
        GlassCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StepBadge(3, isCompleted = true)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Choose Your First Theme",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Selected: ${ThemePresets.getThemeById(config.themeId).name}",
                        fontSize = 12.sp,
                        color = Color(0xFFA0AEC0)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val previewThemes = listOf(
                    ThemePresets.NEON_NIGHT,
                    ThemePresets.CYBERPUNK,
                    ThemePresets.AURORA,
                    ThemePresets.GLASS
                )
                previewThemes.forEach { theme ->
                    val isCurrent = config.themeId == theme.id
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(theme.backgroundColor))
                            .border(
                                width = if (isCurrent) 2.dp else 1.dp,
                                color = if (isCurrent) Color(theme.accentColor) else Color(0x33FFFFFF),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                preferences.updateConfig { it.copy(themeId = theme.id) }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = theme.name.take(6),
                            fontSize = 11.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = Color(theme.textColor)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Step 4: Live Keyboard Test
        GlassCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StepBadge(4, isCompleted = true)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Interactive Live Test",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Tap keys below to test real RGB waves & response",
                        fontSize = 12.sp,
                        color = Color(0xFFA0AEC0)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Text output display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x33000000))
                    .border(1.dp, Color(0x334E5D8F), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = if (testText.isEmpty()) "Tap keyboard below to test typing..." else testText,
                    color = if (testText.isEmpty()) Color(0xFF64748B) else Color(0xFF00F5D4),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Embedded live keyboard preview
            KeyboardLayoutView(
                config = config,
                onKeyPress = { key ->
                    when (key.type) {
                        com.example.model.KeyType.BACKSPACE -> {
                            if (testText.isNotEmpty()) testText = testText.dropLast(1)
                        }
                        com.example.model.KeyType.SPACE -> testText += " "
                        com.example.model.KeyType.ENTER -> testText += "\n"
                        else -> testText += key.primaryChar
                    }
                },
                onBackspaceHold = {
                    if (testText.isNotEmpty()) testText = testText.dropLast(1)
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Get Started Button
        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00F5D4),
                contentColor = Color(0xFF0A0C16)
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Continue to KeyWave Studio →",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun OnboardingStepCard(
    stepNumber: Int,
    title: String,
    description: String,
    isCompleted: Boolean,
    icon: ImageVector,
    actionLabel: String,
    onAction: () -> Unit
) {
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepBadge(stepNumber, isCompleted)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color(0xFFA0AEC0)
                )
            }
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) Color(0x3300F5D4) else Color(0xFF7B2CBF),
                    contentColor = if (isCompleted) Color(0xFF00F5D4) else Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = actionLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StepBadge(number: Int, isCompleted: Boolean) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
                if (isCompleted) Color(0xFF00F5D4) else Color(0x337B2CBF)
            )
            .border(
                1.5.dp,
                if (isCompleted) Color(0xFF00F5D4) else Color(0xFF7B2CBF),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completed",
                tint = Color(0xFF0A0C16),
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = number.toString(),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
