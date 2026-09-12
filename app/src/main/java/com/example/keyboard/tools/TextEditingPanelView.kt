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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeyboardConfig
import com.example.model.KeyboardTheme

@Composable
fun TextEditingPanelView(
    config: KeyboardConfig,
    theme: KeyboardTheme,
    maxHeight: Dp,
    canUndo: Boolean,
    canRedo: Boolean,
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
    onMoveCursorStart: () -> Unit,
    onMoveCursorEnd: () -> Unit,
    onSelectAll: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onPaste: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = config.scaleFactor
    val cardBg = if (config.glassMode) Color(theme.backgroundColor).copy(alpha = 0.85f) else Color(theme.backgroundColor)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(maxHeight)
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, Color(theme.borderColor).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(10.dp)
            .testTag("text_editing_panel_view")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Text Editing & Cursor Pad",
                fontSize = (13 * scale).coerceIn(12f, 15f).sp,
                fontWeight = FontWeight.Bold,
                color = Color(theme.textColor)
            )

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
                    contentDescription = "Close Text Edit",
                    tint = Color(theme.textColor).copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Main Body: Left Side Selection Actions, Right Side D-Pad Navigation
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left Column: Selection & Clipboard Actions
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    ActionPadButton(
                        label = "Select All",
                        icon = Icons.Default.SelectAll,
                        theme = theme,
                        scale = scale,
                        modifier = Modifier.weight(1f),
                        onClick = onSelectAll
                    )
                    ActionPadButton(
                        label = "Copy",
                        icon = Icons.Default.ContentCopy,
                        theme = theme,
                        scale = scale,
                        modifier = Modifier.weight(1f),
                        onClick = onCopy
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    ActionPadButton(
                        label = "Cut",
                        icon = Icons.Default.ContentCut,
                        theme = theme,
                        scale = scale,
                        modifier = Modifier.weight(1f),
                        onClick = onCut
                    )
                    ActionPadButton(
                        label = "Paste",
                        icon = Icons.Default.ContentPaste,
                        theme = theme,
                        scale = scale,
                        modifier = Modifier.weight(1f),
                        onClick = onPaste
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    ActionPadButton(
                        label = "Undo",
                        icon = Icons.AutoMirrored.Filled.Undo,
                        theme = theme,
                        scale = scale,
                        isEnabled = canUndo,
                        modifier = Modifier.weight(1f),
                        onClick = onUndo
                    )
                    ActionPadButton(
                        label = "Redo",
                        icon = Icons.AutoMirrored.Filled.Redo,
                        theme = theme,
                        scale = scale,
                        isEnabled = canRedo,
                        modifier = Modifier.weight(1f),
                        onClick = onRedo
                    )
                }
            }

            // Right Column: Cursor Direction Pad
            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Top: Beginning / Home
                DPadButton(
                    label = "Home",
                    icon = Icons.Default.FirstPage,
                    theme = theme,
                    scale = scale,
                    onClick = onMoveCursorStart
                )

                // Middle: Left & Right
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DPadButton(
                        label = "Left",
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        theme = theme,
                        scale = scale,
                        onClick = onMoveCursorLeft
                    )
                    DPadButton(
                        label = "Right",
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        theme = theme,
                        scale = scale,
                        onClick = onMoveCursorRight
                    )
                }

                // Bottom: End
                DPadButton(
                    label = "End",
                    icon = Icons.Default.LastPage,
                    theme = theme,
                    scale = scale,
                    onClick = onMoveCursorEnd
                )
            }
        }
    }
}

@Composable
private fun ActionPadButton(
    label: String,
    icon: ImageVector,
    theme: KeyboardTheme,
    scale: Float,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val bgColor = if (isEnabled) {
        Color(theme.keyColor).copy(alpha = 0.7f)
    } else {
        Color(theme.keyColor).copy(alpha = 0.3f)
    }
    val contentColor = if (isEnabled) {
        Color(theme.textColor)
    } else {
        Color(theme.textColor).copy(alpha = 0.3f)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, Color(theme.borderColor).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .clickable(enabled = isEnabled) { onClick() }
            .padding(vertical = 8.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isEnabled) Color(theme.accentColor) else contentColor,
            modifier = Modifier.size((14 * scale).coerceIn(12f, 16f).dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = (10 * scale).coerceIn(9f, 12f).sp,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}

@Composable
private fun DPadButton(
    label: String,
    icon: ImageVector,
    theme: KeyboardTheme,
    scale: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size((38 * scale).coerceIn(32f, 44f).dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(theme.specialKeyColor).copy(alpha = 0.8f))
            .border(1.dp, Color(theme.borderColor).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(theme.accentColor),
            modifier = Modifier.size((18 * scale).coerceIn(16f, 22f).dp)
        )
    }
}
