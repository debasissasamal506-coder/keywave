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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.clipboard.ClipboardEntity
import com.example.data.clipboard.ClipboardRepository
import com.example.model.KeyboardConfig
import com.example.model.KeyboardTheme
import kotlinx.coroutines.launch

@Composable
fun ClipboardPanelView(
    repository: ClipboardRepository,
    config: KeyboardConfig,
    theme: KeyboardTheme,
    maxHeight: Dp,
    onTextSelected: (String) -> Unit,
    onToggleSaveHistory: (Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = config.scaleFactor
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }

    val allClips by repository.getAllItems(config.saveClipboardHistory).collectAsState(initial = emptyList())

    val filteredClips = remember(allClips, searchQuery) {
        if (searchQuery.isBlank()) {
            allClips
        } else {
            allClips.filter { it.text.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(maxHeight)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (config.glassMode) Color(theme.backgroundColor).copy(alpha = 0.90f)
                else Color(theme.backgroundColor)
            )
            .border(
                1.dp,
                Color(theme.borderColor).copy(alpha = 0.35f),
                RoundedCornerShape(14.dp)
            )
            .padding(10.dp)
            .testTag("clipboard_panel_view")
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
                    text = "Clipboard Manager",
                    fontSize = (13 * scale).coerceIn(12f, 15f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(theme.textColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "(${allClips.size})",
                    fontSize = 11.sp,
                    color = Color(theme.specialTextColor)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (allClips.any { !it.isPinned }) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(theme.keyColor).copy(alpha = 0.5f))
                            .clickable {
                                scope.launch {
                                    repository.clearUnpinned(config.saveClipboardHistory)
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Clear Unpinned",
                            fontSize = 10.sp,
                            color = Color(0xFFF43F5E),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
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
                        contentDescription = "Close Clipboard",
                        tint = Color(theme.textColor).copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Search Bar & Save History Toggle Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Search Input
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(theme.keyColor).copy(alpha = 0.5f))
                    .border(1.dp, Color(theme.borderColor).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(theme.specialTextColor),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Filter clips...",
                            fontSize = 11.sp,
                            color = Color(theme.specialTextColor).copy(alpha = 0.6f)
                        )
                    }
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color(theme.textColor),
                            fontSize = 11.sp
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Color(theme.specialTextColor),
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { searchQuery = "" }
                    )
                }
            }

            // Save History switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleSaveHistory(!config.saveClipboardHistory) }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Save",
                    fontSize = 10.sp,
                    color = Color(theme.specialTextColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Switch(
                    checked = config.saveClipboardHistory,
                    onCheckedChange = onToggleSaveHistory,
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

        // Clips List
        if (filteredClips.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (searchQuery.isEmpty()) "Clipboard is empty" else "No matching clips found",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(theme.specialTextColor)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Copy text anywhere to see it here",
                        fontSize = 10.sp,
                        color = Color(theme.specialTextColor).copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredClips, key = { it.id }) { clip ->
                    ClipboardItemRow(
                        item = clip,
                        theme = theme,
                        scale = scale,
                        onSelect = { onTextSelected(clip.text) },
                        onTogglePin = {
                            scope.launch {
                                repository.togglePin(clip, config.saveClipboardHistory)
                            }
                        },
                        onDelete = {
                            scope.launch {
                                repository.deleteItem(clip, config.saveClipboardHistory)
                            }
                        }
                    )
                }
            }
        }

        // Privacy Footnote
        Text(
            text = "🔒 Stored locally on device. Never uploaded or synced.",
            fontSize = 9.sp,
            color = Color(theme.specialTextColor).copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 4.dp)
        )
    }
}

@Composable
private fun ClipboardItemRow(
    item: ClipboardEntity,
    theme: KeyboardTheme,
    scale: Float,
    onSelect: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    val rowBg = if (item.isPinned) {
        Color(theme.accentColor).copy(alpha = 0.12f)
    } else {
        Color(theme.keyColor).copy(alpha = 0.55f)
    }

    val borderColor = if (item.isPinned) {
        Color(theme.accentColor).copy(alpha = 0.35f)
    } else {
        Color(theme.borderColor).copy(alpha = 0.18f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(rowBg)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onSelect() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.text,
            fontSize = 11.sp,
            color = Color(theme.textColor),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Pin Button
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable { onTogglePin() }
                .padding(4.dp)
        ) {
            Icon(
                imageVector = if (item.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                contentDescription = if (item.isPinned) "Unpin" else "Pin",
                tint = if (item.isPinned) Color(0xFFF59E0B) else Color(theme.specialTextColor),
                modifier = Modifier.size(15.dp)
            )
        }

        // Delete Button
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable { onDelete() }
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color(theme.specialTextColor).copy(alpha = 0.6f),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
