package com.example.keyboard.tools

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeyboardConfig
import com.example.model.KeyboardLanguage
import com.example.model.KeyboardTheme
import com.example.model.OneHandedMode
import com.example.model.ToolbarTool

@Composable
fun KeyboardToolbarView(
    config: KeyboardConfig,
    theme: KeyboardTheme,
    isToolsPanelOpen: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    isVoiceListening: Boolean,
    onToggleToolsPanel: () -> Unit,
    onToolClick: (ToolbarTool) -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = config.scaleFactor
    val toolbarHeight = (36.dp * scale).coerceIn(32.dp, 44.dp)
    val buttonShape = RoundedCornerShape(8.dp)

    val toolbarBg = when {
        config.glassMode -> Color(theme.keyColor).copy(alpha = 0.40f)
        config.minimalisticMode -> Color(theme.backgroundColor).copy(alpha = 0.85f)
        else -> Color(theme.keyColor).copy(alpha = 0.70f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(toolbarHeight)
            .clip(RoundedCornerShape(10.dp))
            .background(toolbarBg)
            .border(
                1.dp,
                Color(theme.borderColor).copy(alpha = if (config.glassMode) 0.35f else 0.18f),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .testTag("keywave_top_toolbar"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main Menu / Tools Panel Toggle Button (Always accessible)
        ToolbarIconButton(
            icon = Icons.Default.Apps,
            contentDescription = "Open Tools Menu",
            tint = if (isToolsPanelOpen) Color(theme.accentColor) else Color(theme.textColor).copy(alpha = 0.85f),
            backgroundColor = if (isToolsPanelOpen) Color(theme.accentColor).copy(alpha = 0.22f) else Color.Transparent,
            scale = scale,
            onClick = onToggleToolsPanel,
            modifier = Modifier.testTag("toolbar_menu_button")
        )

        // Divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(18.dp * scale)
                .background(Color(theme.borderColor).copy(alpha = 0.35f))
        )

        Spacer(modifier = Modifier.width(2.dp))

        // Horizontal Scrollable Strip of active tools
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            config.visibleToolbarTools.forEach { tool ->
                val isEnabled = when (tool) {
                    ToolbarTool.UNDO -> canUndo
                    ToolbarTool.REDO -> canRedo
                    else -> true
                }

                val badgeText = when (tool) {
                    ToolbarTool.LANGUAGE -> config.currentLanguage.scriptLabel
                    ToolbarTool.ONE_HANDED -> when (config.oneHandedMode) {
                        OneHandedMode.OFF -> null
                        OneHandedMode.LEFT -> "L"
                        OneHandedMode.RIGHT -> "R"
                    }
                    else -> null
                }

                val isHighlighted = when (tool) {
                    ToolbarTool.VOICE -> isVoiceListening
                    ToolbarTool.ONE_HANDED -> config.oneHandedMode != OneHandedMode.OFF
                    else -> false
                }

                ToolbarActionButton(
                    tool = tool,
                    theme = theme,
                    scale = scale,
                    isEnabled = isEnabled,
                    badgeText = badgeText,
                    isHighlighted = isHighlighted,
                    onClick = { if (isEnabled) onToolClick(tool) }
                )
            }
        }
    }
}

@Composable
private fun ToolbarActionButton(
    tool: ToolbarTool,
    theme: KeyboardTheme,
    scale: Float,
    isEnabled: Boolean,
    badgeText: String?,
    isHighlighted: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "toolbar_btn_scale"
    )

    val contentColor = when {
        !isEnabled -> Color(theme.textColor).copy(alpha = 0.30f)
        isHighlighted -> Color(theme.accentColor)
        else -> Color(theme.textColor).copy(alpha = 0.85f)
    }

    val bgColor = when {
        isHighlighted -> Color(theme.accentColor).copy(alpha = 0.20f)
        isPressed -> Color(theme.accentColor).copy(alpha = 0.15f)
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .scale(animatedScale)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .pointerInput(isEnabled) {
                detectTapGestures(
                    onPress = {
                        if (isEnabled) {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }
                    },
                    onTap = {
                        if (isEnabled) onClick()
                    }
                )
            }
            .padding(horizontal = 7.dp, vertical = 4.dp)
            .testTag("toolbar_tool_${tool.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = tool.title,
                tint = contentColor,
                modifier = Modifier.size((16 * scale).coerceIn(14f, 20f).dp)
            )

            if (badgeText != null) {
                Spacer(modifier = Modifier.width(3.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(theme.accentColor).copy(alpha = 0.25f))
                        .padding(horizontal = 3.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = (9 * scale).coerceIn(8f, 11f).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(theme.accentColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolbarIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: Color,
    backgroundColor: Color,
    scale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "icon_btn_scale"
    )

    Box(
        modifier = modifier
            .scale(animatedScale)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size((17 * scale).coerceIn(15f, 22f).dp)
        )
    }
}
