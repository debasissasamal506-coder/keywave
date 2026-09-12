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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.model.ToolbarTool

@Composable
fun ToolbarCustomizationDialog(
    config: KeyboardConfig,
    theme: KeyboardTheme,
    maxHeight: Dp,
    onUpdateConfig: ((KeyboardConfig) -> KeyboardConfig) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = config.scaleFactor
    val allTools = ToolbarTool.values().filter { it != ToolbarTool.CUSTOMIZE_TOOLBAR }

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
            .testTag("toolbar_customization_panel")
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
                text = "Customize Toolbar Shortcuts",
                fontSize = (13 * scale).coerceIn(12f, 15f).sp,
                fontWeight = FontWeight.Bold,
                color = Color(theme.textColor)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Reset Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(theme.keyColor).copy(alpha = 0.6f))
                        .clickable {
                            onUpdateConfig {
                                it.copy(
                                    visibleToolbarTools = listOf(
                                        ToolbarTool.EMOJI,
                                        ToolbarTool.CLIPBOARD,
                                        ToolbarTool.TEXT_EDITING,
                                        ToolbarTool.ONE_HANDED,
                                        ToolbarTool.VOICE,
                                        ToolbarTool.SETTINGS
                                    )
                                )
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Reset",
                        fontSize = 10.sp,
                        color = Color(theme.specialTextColor)
                    )
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

        // List of configurable shortcuts
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(allTools) { index, tool ->
                val isVisible = config.visibleToolbarTools.contains(tool)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(theme.keyColor).copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = tool.title,
                        tint = if (isVisible) Color(theme.accentColor) else Color(theme.specialTextColor),
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tool.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(theme.textColor)
                        )
                        Text(
                            text = tool.description,
                            fontSize = 9.sp,
                            color = Color(theme.specialTextColor)
                        )
                    }

                    // Move Up
                    if (isVisible && config.visibleToolbarTools.indexOf(tool) > 0) {
                        val pos = config.visibleToolbarTools.indexOf(tool)
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    val currentList = config.visibleToolbarTools.toMutableList()
                                    currentList.removeAt(pos)
                                    currentList.add(pos - 1, tool)
                                    onUpdateConfig { it.copy(visibleToolbarTools = currentList) }
                                }
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Move Up",
                                tint = Color(theme.specialTextColor),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Move Down
                    if (isVisible && config.visibleToolbarTools.indexOf(tool) < config.visibleToolbarTools.size - 1) {
                        val pos = config.visibleToolbarTools.indexOf(tool)
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    val currentList = config.visibleToolbarTools.toMutableList()
                                    currentList.removeAt(pos)
                                    currentList.add(pos + 1, tool)
                                    onUpdateConfig { it.copy(visibleToolbarTools = currentList) }
                                }
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Move Down",
                                tint = Color(theme.specialTextColor),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Switch(
                        checked = isVisible,
                        onCheckedChange = { checked ->
                            val currentList = config.visibleToolbarTools.toMutableList()
                            if (checked) {
                                if (!currentList.contains(tool)) currentList.add(tool)
                            } else {
                                currentList.remove(tool)
                            }
                            onUpdateConfig { it.copy(visibleToolbarTools = currentList) }
                        },
                        modifier = Modifier.size(width = 34.dp, height = 20.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(theme.accentColor),
                            checkedTrackColor = Color(theme.accentColor).copy(alpha = 0.4f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.DarkGray
                        )
                    )
                }
            }
        }
    }
}
