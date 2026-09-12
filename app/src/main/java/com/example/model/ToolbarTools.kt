package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolbarTool(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val emojiChar: String
) {
    EMOJI("Emoji", "Browse smileys & symbols", Icons.Default.Mood, "😀"),
    CLIPBOARD("Clipboard", "Saved & recent clips", Icons.Default.ContentPaste, "📋"),
    TEXT_EDITING("Text Edit", "Cursor & selection pad", Icons.Default.Edit, "✏️"),
    ONE_HANDED("One-handed", "Left/right compact mode", Icons.Default.SwapHoriz, "↔️"),
    RESIZE("Resize", "Adjust width & height", Icons.Default.OpenInFull, "↗️"),
    LANGUAGE("Language", "Switch input language", Icons.Default.Language, "🌐"),
    UNDO("Undo", "Revert last edit", Icons.AutoMirrored.Filled.Undo, "↶"),
    REDO("Redo", "Restore undone edit", Icons.AutoMirrored.Filled.Redo, "↷"),
    CUSTOMIZE("Customize", "Colors, RGB & shapes", Icons.Default.Palette, "🎨"),
    VOICE("Voice Input", "Speak to text", Icons.Default.Mic, "🎙"),
    SETTINGS("Settings", "Full preferences", Icons.Default.Settings, "⚙️"),
    CUSTOMIZE_TOOLBAR("Edit Toolbar", "Reorder shortcuts", Icons.Default.Tune, "🛠️")
}

enum class OneHandedMode(val displayName: String) {
    OFF("Off"),
    LEFT("Left"),
    RIGHT("Right")
}

enum class KeyboardLanguage(
    val displayName: String,
    val code: String,
    val scriptLabel: String
) {
    ENGLISH("English", "en", "EN"),
    HINDI("हिंदी", "hi", "HI"),
    BENGALI("বাংলা", "bn", "BN")
}
