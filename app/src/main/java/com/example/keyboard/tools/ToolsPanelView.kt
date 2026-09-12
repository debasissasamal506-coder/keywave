package com.example.keyboard.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeyboardConfig
import com.example.model.KeyboardTheme
import com.example.model.OneHandedMode
import com.example.model.ToolbarTool

@Composable
fun ToolsPanelView(
    config: KeyboardConfig,
    theme: KeyboardTheme,
    maxHeight: Dp,
    onToolSelected: (ToolbarTool) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = config.scaleFactor
    val cardShape = RoundedCornerShape(12.dp)

    val allTools = ToolbarTool.values().toList()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(maxHeight)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (config.glassMode) Color(theme.backgroundColor).copy(alpha = 0.85f)
                else Color(theme.backgroundColor)
            )
            .border(
                1.dp,
                Color(theme.borderColor).copy(alpha = 0.35f),
                RoundedCornerShape(14.dp)
            )
            .padding(10.dp)
            .testTag("tools_panel_view")
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
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(theme.accentColor))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "KeyWave Tools",
                    fontSize = (14 * scale).coerceIn(12f, 16f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(theme.textColor)
                )
            }

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
                    contentDescription = "Close Tools Panel",
                    tint = Color(theme.textColor).copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Grid of tools cards
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(allTools) { tool ->
                ToolCardItem(
                    tool = tool,
                    config = config,
                    theme = theme,
                    scale = scale,
                    shape = cardShape,
                    onClick = { onToolSelected(tool) }
                )
            }
        }
    }
}

@Composable
private fun ToolCardItem(
    tool: ToolbarTool,
    config: KeyboardConfig,
    theme: KeyboardTheme,
    scale: Float,
    shape: androidx.compose.ui.graphics.Shape,
    onClick: () -> Unit
) {
    val subText = when (tool) {
        ToolbarTool.LANGUAGE -> config.currentLanguage.displayName
        ToolbarTool.ONE_HANDED -> config.oneHandedMode.displayName
        ToolbarTool.RESIZE -> "${config.keyboardSizePercent}%"
        else -> null
    }

    val cardBg = if (config.glassMode) {
        Color(theme.keyColor).copy(alpha = 0.45f)
    } else {
        Color(theme.keyColor).copy(alpha = 0.65f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(cardBg)
            .border(
                1.dp,
                Color(theme.borderColor).copy(alpha = 0.25f),
                shape
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .testTag("tool_card_${tool.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size((28 * scale).coerceIn(24f, 34f).dp)
                    .clip(CircleShape)
                    .background(Color(theme.accentColor).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.title,
                    tint = Color(theme.accentColor),
                    modifier = Modifier.size((16 * scale).coerceIn(14f, 20f).dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = tool.title,
                fontSize = (10 * scale).coerceIn(9f, 12f).sp,
                fontWeight = FontWeight.Medium,
                color = Color(theme.textColor),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (subText != null) {
                Text(
                    text = subText,
                    fontSize = (8 * scale).coerceIn(7f, 10f).sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(theme.specialTextColor).copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
