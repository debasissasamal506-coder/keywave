package com.example.keyboard

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.example.data.clipboard.ClipboardRepository
import com.example.keyboard.tools.ClipboardPanelView
import com.example.keyboard.tools.KeyboardResizeDialog
import com.example.keyboard.tools.KeyboardToolbarView
import com.example.keyboard.tools.TextEditingPanelView
import com.example.keyboard.tools.ToolbarCustomizationDialog
import com.example.keyboard.tools.ToolsPanelView
import com.example.keyboard.tools.VoiceInputHelper
import com.example.model.KeyboardLanguage
import com.example.model.LanguageLayouts
import com.example.model.OneHandedMode
import com.example.model.ToolbarTool
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameMillis
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeyItem
import com.example.model.KeyShape
import com.example.model.KeyType
import com.example.model.KeyboardConfig
import com.example.model.KeyboardLayoutMode
import com.example.model.KeyboardLayouts
import com.example.model.KeyboardTheme
import com.example.model.RGBMode
import com.example.model.ThemePresets
import com.example.model.WaveColorMode
import com.example.model.WaveIntensity
import com.example.model.WaveSpeed
import com.example.model.WordDictionary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class ActiveWave(
    val originRow: Int,
    val originCol: Int,
    val timestamp: Long
)

enum class ActiveSubpanel {
    NONE,
    TOOLS_GRID,
    CLIPBOARD,
    TEXT_EDIT,
    RESIZE,
    CUSTOMIZE_TOOLBAR
}

@Composable
fun KeyboardLayoutView(
    config: KeyboardConfig,
    onKeyPress: (KeyItem) -> Unit,
    onBackspaceHold: () -> Unit = {},
    onBackspaceRelease: () -> Unit = {},
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    currentWordPrefix: String = "",
    onSuggestionSelected: (String) -> Unit = {},
    onUpdateConfig: ((KeyboardConfig) -> KeyboardConfig) -> Unit = {},
    onNavigateToCustomize: (() -> Unit)? = null,
    onNavigateToSettings: (() -> Unit)? = null,
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
    onMoveCursorLeft: () -> Unit = {},
    onMoveCursorRight: () -> Unit = {},
    onMoveCursorStart: () -> Unit = {},
    onMoveCursorEnd: () -> Unit = {},
    onSelectAll: () -> Unit = {},
    onCopy: () -> Unit = {},
    onCut: () -> Unit = {},
    onPaste: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardRepo = remember(context) { ClipboardRepository.getInstance(context) }
    var isVoiceListening by remember { mutableStateOf(false) }
    val voiceHelper = remember(context) {
        VoiceInputHelper(
            context = context,
            onResult = { text ->
                isVoiceListening = false
                onKeyPress(KeyItem(text))
            },
            onStatus = { status ->
                isVoiceListening = status.contains("Listening", ignoreCase = true)
            }
        )
    }

    DisposableEffect(voiceHelper) {
        onDispose {
            voiceHelper.stopListening()
        }
    }

    var activeSubpanel by remember { mutableStateOf(ActiveSubpanel.NONE) }

    val theme = remember(
        config.themeId,
        config.useCustomColors,
        config.customBackground,
        config.customKeyColor,
        config.customTextColor,
        config.customAccent,
        config.customGlowColor,
        config.customSpecialKeyColor,
        config.customSecondary,
        config.customSecondaryText,
        config.rgbMode
    ) {
        if (config.useCustomColors) {
            KeyboardTheme(
                id = "custom",
                name = "Custom",
                backgroundColor = config.customBackground,
                keyColor = config.customKeyColor,
                keyActiveColor = config.customAccent,
                specialKeyColor = config.customSpecialKeyColor,
                textColor = config.customTextColor,
                specialTextColor = config.customAccent,
                accentColor = config.customAccent,
                secondaryColor = config.customSecondary,
                glowColor = config.customGlowColor,
                borderColor = config.customSecondaryText,
                defaultRgbMode = config.rgbMode
            )
        } else {
            ThemePresets.getThemeById(config.themeId)
        }
    }

    var layoutMode by remember { mutableStateOf(KeyboardLayoutMode.LETTERS) }
    var isShifted by remember { mutableStateOf(false) }
    var isCapsLock by remember { mutableStateOf(false) }
    var lastShiftClickTime by remember { mutableLongStateOf(0L) }

    // Wave animation tracking
    val activeWaves = remember { mutableStateListOf<ActiveWave>() }
    val coroutineScope = rememberCoroutineScope()

    var waveTick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(activeWaves.size) {
        if (activeWaves.isNotEmpty()) {
            while (activeWaves.isNotEmpty()) {
                withFrameMillis {
                    val now = SystemClock.uptimeMillis()
                    waveTick = now
                    activeWaves.removeAll { now - it.timestamp > 1000L }
                }
            }
            waveTick = 0L
        }
    }

    // Ambient continuous rainbow / breathing phase
    val infiniteTransition = rememberInfiniteTransition(label = "rgb_ambient")
    val ambientPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambient_phase"
    )

    // Key shape configuration
    val keyShapeRadius = remember(config.keyShape) {
        when (config.keyShape) {
            KeyShape.ROUNDED -> 10.dp
            KeyShape.SOFT_ROUNDED -> 6.dp
            KeyShape.PILL -> 999.dp
            KeyShape.MINIMAL -> 2.dp
        }
    }
    val shape: Shape = remember(keyShapeRadius) { RoundedCornerShape(keyShapeRadius) }

    // Keyboard sizing calculations
    val scale = config.scaleFactor
    val baseKeyHeight = config.keyHeightDp.dp * scale

    fun handleToolbarToolClick(tool: ToolbarTool) {
        when (tool) {
            ToolbarTool.EMOJI -> {
                activeSubpanel = ActiveSubpanel.NONE
                layoutMode = if (layoutMode == KeyboardLayoutMode.EMOJIS) KeyboardLayoutMode.LETTERS else KeyboardLayoutMode.EMOJIS
            }
            ToolbarTool.CLIPBOARD -> {
                activeSubpanel = if (activeSubpanel == ActiveSubpanel.CLIPBOARD) ActiveSubpanel.NONE else ActiveSubpanel.CLIPBOARD
            }
            ToolbarTool.TEXT_EDITING -> {
                activeSubpanel = if (activeSubpanel == ActiveSubpanel.TEXT_EDIT) ActiveSubpanel.NONE else ActiveSubpanel.TEXT_EDIT
            }
            ToolbarTool.ONE_HANDED -> {
                val nextMode = when (config.oneHandedMode) {
                    OneHandedMode.OFF -> OneHandedMode.RIGHT
                    OneHandedMode.RIGHT -> OneHandedMode.LEFT
                    OneHandedMode.LEFT -> OneHandedMode.OFF
                }
                onUpdateConfig { it.copy(oneHandedMode = nextMode) }
            }
            ToolbarTool.RESIZE -> {
                activeSubpanel = if (activeSubpanel == ActiveSubpanel.RESIZE) ActiveSubpanel.NONE else ActiveSubpanel.RESIZE
            }
            ToolbarTool.LANGUAGE -> {
                val nextLang = when (config.currentLanguage) {
                    KeyboardLanguage.ENGLISH -> KeyboardLanguage.HINDI
                    KeyboardLanguage.HINDI -> KeyboardLanguage.BENGALI
                    KeyboardLanguage.BENGALI -> KeyboardLanguage.ENGLISH
                }
                onUpdateConfig { it.copy(currentLanguage = nextLang) }
            }
            ToolbarTool.UNDO -> onUndo()
            ToolbarTool.REDO -> onRedo()
            ToolbarTool.SETTINGS -> {
                if (onNavigateToSettings != null) {
                    onNavigateToSettings()
                } else {
                    activeSubpanel = ActiveSubpanel.NONE
                    onKeyPress(KeyItem("⚙️"))
                }
            }
            ToolbarTool.CUSTOMIZE -> {
                if (onNavigateToCustomize != null) {
                    onNavigateToCustomize()
                } else {
                    activeSubpanel = ActiveSubpanel.RESIZE
                }
            }
            ToolbarTool.VOICE -> {
                if (isVoiceListening) {
                    voiceHelper.stopListening()
                    isVoiceListening = false
                } else {
                    voiceHelper.startListening()
                }
            }
            ToolbarTool.CUSTOMIZE_TOOLBAR -> {
                activeSubpanel = if (activeSubpanel == ActiveSubpanel.CUSTOMIZE_TOOLBAR) ActiveSubpanel.NONE else ActiveSubpanel.CUSTOMIZE_TOOLBAR
            }
        }
    }

    // Color tokens
    val bgColor = remember(theme.backgroundColor, config.minimalisticMode, config.glassMode) {
        when {
            config.glassMode -> Color(theme.backgroundColor).copy(alpha = 0.50f)
            config.minimalisticMode -> Color(theme.backgroundColor)
            else -> Color(theme.backgroundColor)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .testTag("keywave_keyboard_view")
    ) {
        val totalKeyboardWidth = maxWidth

        // Dynamic side margins that adapt gracefully if rendered in a narrow preview or split-screen
        val sidePadding = remember(totalKeyboardWidth, config.sideMargins) {
            val configured = config.sideMargins.paddingDp.dp
            if (totalKeyboardWidth < 360.dp) {
                minOf(configured, totalKeyboardWidth * 0.03f)
            } else {
                configured
            }
        }

        // Dynamic inter-key gap scaled to screen bounds and user configuration
        val keyGap = remember(totalKeyboardWidth, scale, config.keySpacingDp) {
            (config.keySpacingDp.dp * scale).coerceIn(2.dp, 6.dp)
        }

        val rowSpacing = (5.dp * scale).coerceIn(3.dp, 6.dp)
        val subpanelHeight = (baseKeyHeight * (if (config.showNumberRow) 4.6f else 4.0f)) + (rowSpacing * 4)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = sidePadding, vertical = 3.dp * scale)
        ) {
            // Top Toolbar (Requirement 32)
            if (config.showToolbar) {
                KeyboardToolbarView(
                    config = config,
                    theme = theme,
                    isToolsPanelOpen = activeSubpanel == ActiveSubpanel.TOOLS_GRID,
                    canUndo = canUndo,
                    canRedo = canRedo,
                    isVoiceListening = isVoiceListening,
                    onToggleToolsPanel = {
                        activeSubpanel = if (activeSubpanel == ActiveSubpanel.TOOLS_GRID) ActiveSubpanel.NONE else ActiveSubpanel.TOOLS_GRID
                    },
                    onToolClick = { tool ->
                        handleToolbarToolClick(tool)
                    }
                )
                Spacer(modifier = Modifier.height(3.dp * scale))
            }

            // Suggestion Bar (Requirement 13)
            if (config.showSuggestions && layoutMode == KeyboardLayoutMode.LETTERS && activeSubpanel == ActiveSubpanel.NONE) {
                val suggestions = remember(currentWordPrefix) {
                    WordDictionary.getSuggestions(currentWordPrefix)
                }
                SuggestionBar(
                    suggestions = suggestions,
                    theme = theme,
                    config = config,
                    onSuggestionClick = { word ->
                        onSuggestionSelected(word)
                    }
                )
                Spacer(modifier = Modifier.height(3.dp * scale))
            }

            val keyboardContent: @Composable () -> Unit = {
                if (activeSubpanel != ActiveSubpanel.NONE) {
                    when (activeSubpanel) {
                        ActiveSubpanel.TOOLS_GRID -> {
                            ToolsPanelView(
                                config = config,
                                theme = theme,
                                maxHeight = subpanelHeight,
                                onToolSelected = { tool -> handleToolbarToolClick(tool) },
                                onClose = { activeSubpanel = ActiveSubpanel.NONE }
                            )
                        }
                        ActiveSubpanel.CLIPBOARD -> {
                            ClipboardPanelView(
                                repository = clipboardRepo,
                                config = config,
                                theme = theme,
                                maxHeight = subpanelHeight,
                                onTextSelected = { text ->
                                    onKeyPress(KeyItem(text))
                                    activeSubpanel = ActiveSubpanel.NONE
                                },
                                onToggleSaveHistory = { enabled ->
                                    onUpdateConfig { it.copy(saveClipboardHistory = enabled) }
                                },
                                onClose = { activeSubpanel = ActiveSubpanel.NONE }
                            )
                        }
                        ActiveSubpanel.TEXT_EDIT -> {
                            TextEditingPanelView(
                                config = config,
                                theme = theme,
                                maxHeight = subpanelHeight,
                                canUndo = canUndo,
                                canRedo = canRedo,
                                onMoveCursorLeft = onMoveCursorLeft,
                                onMoveCursorRight = onMoveCursorRight,
                                onMoveCursorStart = onMoveCursorStart,
                                onMoveCursorEnd = onMoveCursorEnd,
                                onSelectAll = onSelectAll,
                                onCopy = onCopy,
                                onCut = onCut,
                                onPaste = onPaste,
                                onUndo = onUndo,
                                onRedo = onRedo,
                                onClose = { activeSubpanel = ActiveSubpanel.NONE }
                            )
                        }
                        ActiveSubpanel.RESIZE -> {
                            KeyboardResizeDialog(
                                config = config,
                                theme = theme,
                                maxHeight = subpanelHeight,
                                onUpdateConfig = onUpdateConfig,
                                onClose = { activeSubpanel = ActiveSubpanel.NONE }
                            )
                        }
                        ActiveSubpanel.CUSTOMIZE_TOOLBAR -> {
                            ToolbarCustomizationDialog(
                                config = config,
                                theme = theme,
                                maxHeight = subpanelHeight,
                                onUpdateConfig = onUpdateConfig,
                                onClose = { activeSubpanel = ActiveSubpanel.NONE }
                            )
                        }
                        ActiveSubpanel.NONE -> Unit
                    }
                } else {
                    when (layoutMode) {
                        KeyboardLayoutMode.LETTERS -> {
                            when (config.currentLanguage) {
                                KeyboardLanguage.HINDI -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(rowSpacing)
                                    ) {
                                        if (config.showNumberRow) {
                                            KeyboardRow(
                                                keys = KeyboardLayouts.NUMBER_ROW,
                                                rowIdx = 0,
                                                keyHeight = baseKeyHeight * 0.88f,
                                                shape = shape,
                                                theme = theme,
                                                config = config,
                                                activeWaves = activeWaves,
                                                ambientPhase = ambientPhase,
                                                scale = scale,
                                                isShifted = false,
                                                keyGap = keyGap,
                                                waveTick = waveTick,
                                                onKeyClick = { onKeyPress(it) }
                                            )
                                        }

                                        KeyboardRow(
                                            keys = LanguageLayouts.HINDI_ROW_1,
                                            rowIdx = 1,
                                            keyHeight = baseKeyHeight,
                                            shape = shape,
                                            theme = theme,
                                            config = config,
                                            activeWaves = activeWaves,
                                            ambientPhase = ambientPhase,
                                            scale = scale,
                                            isShifted = isShifted || isCapsLock,
                                            keyGap = keyGap,
                                            waveTick = waveTick,
                                            onKeyClick = { key ->
                                                val actualChar = if (isShifted || isCapsLock) key.shiftChar else key.primaryChar
                                                onKeyPress(key.copy(primaryChar = actualChar))
                                                if (isShifted && !isCapsLock) isShifted = false
                                                triggerWave(1, key.col, theme.accentColor, activeWaves, config)
                                            }
                                        )

                                        KeyboardRow(
                                            keys = LanguageLayouts.HINDI_ROW_2,
                                            rowIdx = 2,
                                            keyHeight = baseKeyHeight,
                                            shape = shape,
                                            theme = theme,
                                            config = config,
                                            activeWaves = activeWaves,
                                            ambientPhase = ambientPhase,
                                            scale = scale,
                                            isShifted = isShifted || isCapsLock,
                                            keyGap = keyGap,
                                            waveTick = waveTick,
                                            onKeyClick = { key ->
                                                val actualChar = if (isShifted || isCapsLock) key.shiftChar else key.primaryChar
                                                onKeyPress(key.copy(primaryChar = actualChar))
                                                if (isShifted && !isCapsLock) isShifted = false
                                                triggerWave(2, key.col, theme.accentColor, activeWaves, config)
                                            }
                                        )

                                        val hindiRow3 = remember(isShifted, isCapsLock) {
                                            LanguageLayouts.getHindiRow3(isShifted || isCapsLock)
                                        }
                                        KeyboardRow(
                                            keys = hindiRow3,
                                            rowIdx = 3,
                                            keyHeight = baseKeyHeight,
                                            shape = shape,
                                            theme = theme,
                                            config = config,
                                            activeWaves = activeWaves,
                                            ambientPhase = ambientPhase,
                                            scale = scale,
                                            isShifted = isShifted || isCapsLock,
                                            keyGap = keyGap,
                                            waveTick = waveTick,
                                            onKeyClick = { key ->
                                                when (key.type) {
                                                    KeyType.SHIFT -> {
                                                        val now = SystemClock.uptimeMillis()
                                                        if (now - lastShiftClickTime < 350) {
                                                            isCapsLock = !isCapsLock
                                                            isShifted = isCapsLock
                                                        } else {
                                                            if (isCapsLock) {
                                                                isCapsLock = false
                                                                isShifted = false
                                                            } else {
                                                                isShifted = !isShifted
                                                            }
                                                        }
                                                        lastShiftClickTime = now
                                                    }
                                                    KeyType.BACKSPACE -> onKeyPress(key)
                                                    else -> {
                                                        val actualChar = if (isShifted || isCapsLock) key.shiftChar else key.primaryChar
                                                        onKeyPress(key.copy(primaryChar = actualChar))
                                                        if (isShifted && !isCapsLock) isShifted = false
                                                    }
                                                }
                                                triggerWave(3, key.col, theme.accentColor, activeWaves, config)
                                            },
                                            onBackspaceHold = onBackspaceHold,
                                            onBackspaceRelease = onBackspaceRelease
                                        )

                                        KeyboardRow(
                                            keys = LanguageLayouts.HINDI_BOTTOM_ROW,
                                            rowIdx = 4,
                                            keyHeight = baseKeyHeight,
                                            shape = shape,
                                            theme = theme,
                                            config = config,
                                            activeWaves = activeWaves,
                                            ambientPhase = ambientPhase,
                                            scale = scale,
                                            isShifted = false,
                                            keyGap = keyGap,
                                            waveTick = waveTick,
                                            onKeyClick = { key ->
                                                when (key.type) {
                                                    KeyType.SWITCH_SYMBOLS -> layoutMode = KeyboardLayoutMode.SYMBOLS
                                                    KeyType.EMOJI -> layoutMode = KeyboardLayoutMode.EMOJIS
                                                    else -> onKeyPress(key)
                                                }
                                                triggerWave(4, key.col, theme.accentColor, activeWaves, config)
                                            }
                                        )
                                    }
                                }
                                KeyboardLanguage.BENGALI -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(rowSpacing)
                                    ) {
                                        if (config.showNumberRow) {
                                            KeyboardRow(
                                                keys = KeyboardLayouts.NUMBER_ROW,
                                                rowIdx = 0,
                                                keyHeight = baseKeyHeight * 0.88f,
                                                shape = shape,
                                                theme = theme,
                                                config = config,
                                                activeWaves = activeWaves,
                                                ambientPhase = ambientPhase,
                                                scale = scale,
                                                isShifted = false,
                                                keyGap = keyGap,
                                                waveTick = waveTick,
                                                onKeyClick = { onKeyPress(it) }
                                            )
                                        }

                                        KeyboardRow(
                                            keys = LanguageLayouts.BENGALI_ROW_1,
                                            rowIdx = 1,
                                            keyHeight = baseKeyHeight,
                                            shape = shape,
                                            theme = theme,
                                            config = config,
                                            activeWaves = activeWaves,
                                            ambientPhase = ambientPhase,
                                            scale = scale,
                                            isShifted = isShifted || isCapsLock,
                                            keyGap = keyGap,
                                            waveTick = waveTick,
                                            onKeyClick = { key ->
                                                val actualChar = if (isShifted || isCapsLock) key.shiftChar else key.primaryChar
                                                onKeyPress(key.copy(primaryChar = actualChar))
                                                if (isShifted && !isCapsLock) isShifted = false
                                                triggerWave(1, key.col, theme.accentColor, activeWaves, config)
                                            }
                                        )

                                        KeyboardRow(
                                            keys = LanguageLayouts.BENGALI_ROW_2,
                                            rowIdx = 2,
                                            keyHeight = baseKeyHeight,
                                            shape = shape,
                                            theme = theme,
                                            config = config,
                                            activeWaves = activeWaves,
                                            ambientPhase = ambientPhase,
                                            scale = scale,
                                            isShifted = isShifted || isCapsLock,
                                            keyGap = keyGap,
                                            waveTick = waveTick,
                                            onKeyClick = { key ->
                                                val actualChar = if (isShifted || isCapsLock) key.shiftChar else key.primaryChar
                                                onKeyPress(key.copy(primaryChar = actualChar))
                                                if (isShifted && !isCapsLock) isShifted = false
                                                triggerWave(2, key.col, theme.accentColor, activeWaves, config)
                                            }
                                        )

                                        val bengaliRow3 = remember(isShifted, isCapsLock) {
                                            LanguageLayouts.getBengaliRow3(isShifted || isCapsLock)
                                        }
                                        KeyboardRow(
                                            keys = bengaliRow3,
                                            rowIdx = 3,
                                            keyHeight = baseKeyHeight,
                                            shape = shape,
                                            theme = theme,
                                            config = config,
                                            activeWaves = activeWaves,
                                            ambientPhase = ambientPhase,
                                            scale = scale,
                                            isShifted = isShifted || isCapsLock,
                                            keyGap = keyGap,
                                            waveTick = waveTick,
                                            onKeyClick = { key ->
                                                when (key.type) {
                                                    KeyType.SHIFT -> {
                                                        val now = SystemClock.uptimeMillis()
                                                        if (now - lastShiftClickTime < 350) {
                                                            isCapsLock = !isCapsLock
                                                            isShifted = isCapsLock
                                                        } else {
                                                            if (isCapsLock) {
                                                                isCapsLock = false
                                                                isShifted = false
                                                            } else {
                                                                isShifted = !isShifted
                                                            }
                                                        }
                                                        lastShiftClickTime = now
                                                    }
                                                    KeyType.BACKSPACE -> onKeyPress(key)
                                                    else -> {
                                                        val actualChar = if (isShifted || isCapsLock) key.shiftChar else key.primaryChar
                                                        onKeyPress(key.copy(primaryChar = actualChar))
                                                        if (isShifted && !isCapsLock) isShifted = false
                                                    }
                                                }
                                                triggerWave(3, key.col, theme.accentColor, activeWaves, config)
                                            },
                                            onBackspaceHold = onBackspaceHold,
                                            onBackspaceRelease = onBackspaceRelease
                                        )

                                        KeyboardRow(
                                            keys = LanguageLayouts.BENGALI_BOTTOM_ROW,
                                            rowIdx = 4,
                                            keyHeight = baseKeyHeight,
                                            shape = shape,
                                            theme = theme,
                                            config = config,
                                            activeWaves = activeWaves,
                                            ambientPhase = ambientPhase,
                                            scale = scale,
                                            isShifted = false,
                                            keyGap = keyGap,
                                            waveTick = waveTick,
                                            onKeyClick = { key ->
                                                when (key.type) {
                                                    KeyType.SWITCH_SYMBOLS -> layoutMode = KeyboardLayoutMode.SYMBOLS
                                                    KeyType.EMOJI -> layoutMode = KeyboardLayoutMode.EMOJIS
                                                    else -> onKeyPress(key)
                                                }
                                                triggerWave(4, key.col, theme.accentColor, activeWaves, config)
                                            }
                                        )
                                    }
                                }
                                KeyboardLanguage.ENGLISH -> {
                                    // Default English QWERTY
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(rowSpacing)
                                    ) {
                                        if (config.showNumberRow) {
                                            KeyboardRow(
                                                keys = KeyboardLayouts.NUMBER_ROW,
                                                rowIdx = 0,
                                                keyHeight = baseKeyHeight * 0.88f,
                                                shape = shape,
                                                theme = theme,
                                                config = config,
                                                activeWaves = activeWaves,
                                                ambientPhase = ambientPhase,
                                                scale = scale,
                                                isShifted = false,
                                                keyGap = keyGap,
                                                waveTick = waveTick,
                                                onKeyClick = { onKeyPress(it) }
                                            )
                                        }

                                        // Row 1: QWERTYUIOP
                                        KeyboardRow(
                                            keys = KeyboardLayouts.QWERTY_ROW_1,
                                            rowIdx = 1,
                                            keyHeight = baseKeyHeight,
                                            shape = shape,
                                            theme = theme,
                                            config = config,
                                            activeWaves = activeWaves,
                                            ambientPhase = ambientPhase,
                                            scale = scale,
                                            isShifted = isShifted || isCapsLock,
                                            keyGap = keyGap,
                                            waveTick = waveTick,
                                            onKeyClick = { key ->
                                                val actualChar = if (isShifted || isCapsLock) key.shiftChar else key.primaryChar
                                                onKeyPress(key.copy(primaryChar = actualChar))
                                                if (isShifted && !isCapsLock) isShifted = false
                                                triggerWave(1, key.col, theme.accentColor, activeWaves, config)
                                            }
                                        )

                                        // Row 2: ASDFGHJKL (centered with equal side padding for clean standard keyboard geometry)
                                        KeyboardRow(
                                            keys = KeyboardLayouts.QWERTY_ROW_2,
                                            rowIdx = 2,
                                            keyHeight = baseKeyHeight,
                                            shape = shape,
                                            theme = theme,
                                            config = config,
                                            activeWaves = activeWaves,
                                            ambientPhase = ambientPhase,
                                            scale = scale,
                                            isShifted = isShifted || isCapsLock,
                                            sideMarginPercent = 0.5f,
                                            keyGap = keyGap,
                                            waveTick = waveTick,
                                            onKeyClick = { key ->
                                                val actualChar = if (isShifted || isCapsLock) key.shiftChar else key.primaryChar
                                                onKeyPress(key.copy(primaryChar = actualChar))
                                                if (isShifted && !isCapsLock) isShifted = false
                                                triggerWave(2, key.col, theme.accentColor, activeWaves, config)
                                            }
                                        )

                                        // Row 3: Shift + ZXCVBNM + Backspace
                                        val row3Keys = remember(isShifted, isCapsLock) {
                                            KeyboardLayouts.getQwertyRow3(isShifted, isCapsLock)
                                        }
                                        KeyboardRow(
                                            keys = row3Keys,
                                            rowIdx = 3,
                                            keyHeight = baseKeyHeight,
                                            shape = shape,
                                            theme = theme,
                                            config = config,
                                            activeWaves = activeWaves,
                                            ambientPhase = ambientPhase,
                                            scale = scale,
                                            isShifted = isShifted || isCapsLock,
                                            keyGap = keyGap,
                                            waveTick = waveTick,
                                            onKeyClick = { key ->
                                                when (key.type) {
                                                    KeyType.SHIFT -> {
                                                        val now = SystemClock.uptimeMillis()
                                                        if (now - lastShiftClickTime < 350) {
                                                            isCapsLock = !isCapsLock
                                                            isShifted = isCapsLock
                                                        } else {
                                                            if (isCapsLock) {
                                                                isCapsLock = false
                                                                isShifted = false
                                                            } else {
                                                                isShifted = !isShifted
                                                            }
                                                        }
                                                        lastShiftClickTime = now
                                                    }
                                                    KeyType.BACKSPACE -> {
                                                        onKeyPress(key)
                                                    }
                                                    else -> {
                                                        val actualChar = if (isShifted || isCapsLock) key.shiftChar else key.primaryChar
                                                        onKeyPress(key.copy(primaryChar = actualChar))
                                                        if (isShifted && !isCapsLock) isShifted = false
                                                    }
                                                }
                                                triggerWave(3, key.col, theme.accentColor, activeWaves, config)
                                            },
                                            onBackspaceHold = onBackspaceHold,
                                            onBackspaceRelease = onBackspaceRelease
                                        )

                                        // Row 4: ?123, Emoji, comma, space, period, enter
                                        KeyboardRow(
                                            keys = KeyboardLayouts.QWERTY_BOTTOM_ROW,
                                            rowIdx = 4,
                                            keyHeight = baseKeyHeight,
                                            shape = shape,
                                            theme = theme,
                                            config = config,
                                            activeWaves = activeWaves,
                                            ambientPhase = ambientPhase,
                                            scale = scale,
                                            isShifted = false,
                                            keyGap = keyGap,
                                            waveTick = waveTick,
                                            onKeyClick = { key ->
                                                when (key.type) {
                                                    KeyType.SWITCH_SYMBOLS -> {
                                                        layoutMode = KeyboardLayoutMode.SYMBOLS
                                                    }
                                                    KeyType.EMOJI -> {
                                                        layoutMode = KeyboardLayoutMode.EMOJIS
                                                    }
                                                    else -> {
                                                        onKeyPress(key)
                                                    }
                                                }
                                                triggerWave(4, key.col, theme.accentColor, activeWaves, config)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        KeyboardLayoutMode.SYMBOLS -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(rowSpacing)
                            ) {
                                KeyboardRow(
                                    keys = KeyboardLayouts.SYMBOLS_ROW_1,
                                    rowIdx = 0,
                                    keyHeight = baseKeyHeight,
                                    shape = shape,
                                    theme = theme,
                                    config = config,
                                    activeWaves = activeWaves,
                                    ambientPhase = ambientPhase,
                                    scale = scale,
                                    isShifted = false,
                                    keyGap = keyGap,
                                    waveTick = waveTick,
                                    onKeyClick = { onKeyPress(it) }
                                )

                                KeyboardRow(
                                    keys = KeyboardLayouts.SYMBOLS_ROW_2,
                                    rowIdx = 1,
                                    keyHeight = baseKeyHeight,
                                    shape = shape,
                                    theme = theme,
                                    config = config,
                                    activeWaves = activeWaves,
                                    ambientPhase = ambientPhase,
                                    scale = scale,
                                    isShifted = false,
                                    keyGap = keyGap,
                                    waveTick = waveTick,
                                    onKeyClick = { onKeyPress(it) }
                                )

                                KeyboardRow(
                                    keys = KeyboardLayouts.SYMBOLS_ROW_3,
                                    rowIdx = 2,
                                    keyHeight = baseKeyHeight,
                                    shape = shape,
                                    theme = theme,
                                    config = config,
                                    activeWaves = activeWaves,
                                    ambientPhase = ambientPhase,
                                    scale = scale,
                                    isShifted = false,
                                    keyGap = keyGap,
                                    waveTick = waveTick,
                                    onKeyClick = { key ->
                                        if (key.type == KeyType.SWITCH_EXTRA_SYMBOLS) {
                                            layoutMode = KeyboardLayoutMode.EXTRA_SYMBOLS
                                        } else {
                                            onKeyPress(key)
                                        }
                                    },
                                    onBackspaceHold = onBackspaceHold,
                                    onBackspaceRelease = onBackspaceRelease
                                )

                                KeyboardRow(
                                    keys = KeyboardLayouts.SYMBOLS_BOTTOM_ROW,
                                    rowIdx = 3,
                                    keyHeight = baseKeyHeight,
                                    shape = shape,
                                    theme = theme,
                                    config = config,
                                    activeWaves = activeWaves,
                                    ambientPhase = ambientPhase,
                                    scale = scale,
                                    isShifted = false,
                                    keyGap = keyGap,
                                    waveTick = waveTick,
                                    onKeyClick = { key ->
                                        when (key.type) {
                                            KeyType.SWITCH_LETTERS -> layoutMode = KeyboardLayoutMode.LETTERS
                                            KeyType.EMOJI -> layoutMode = KeyboardLayoutMode.EMOJIS
                                            else -> onKeyPress(key)
                                        }
                                    }
                                )
                            }
                        }

                        KeyboardLayoutMode.EXTRA_SYMBOLS -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(rowSpacing)
                            ) {
                                KeyboardRow(
                                    keys = KeyboardLayouts.EXTRA_SYMBOLS_ROW_1,
                                    rowIdx = 0,
                                    keyHeight = baseKeyHeight,
                                    shape = shape,
                                    theme = theme,
                                    config = config,
                                    activeWaves = activeWaves,
                                    ambientPhase = ambientPhase,
                                    scale = scale,
                                    isShifted = false,
                                    keyGap = keyGap,
                                    waveTick = waveTick,
                                    onKeyClick = { onKeyPress(it) }
                                )

                                KeyboardRow(
                                    keys = KeyboardLayouts.EXTRA_SYMBOLS_ROW_2,
                                    rowIdx = 1,
                                    keyHeight = baseKeyHeight,
                                    shape = shape,
                                    theme = theme,
                                    config = config,
                                    activeWaves = activeWaves,
                                    ambientPhase = ambientPhase,
                                    scale = scale,
                                    isShifted = false,
                                    keyGap = keyGap,
                                    waveTick = waveTick,
                                    onKeyClick = { onKeyPress(it) }
                                )

                                KeyboardRow(
                                    keys = KeyboardLayouts.EXTRA_SYMBOLS_ROW_3,
                                    rowIdx = 2,
                                    keyHeight = baseKeyHeight,
                                    shape = shape,
                                    theme = theme,
                                    config = config,
                                    activeWaves = activeWaves,
                                    ambientPhase = ambientPhase,
                                    scale = scale,
                                    isShifted = false,
                                    keyGap = keyGap,
                                    waveTick = waveTick,
                                    onKeyClick = { key ->
                                        if (key.type == KeyType.SWITCH_SYMBOLS) {
                                            layoutMode = KeyboardLayoutMode.SYMBOLS
                                        } else {
                                            onKeyPress(key)
                                        }
                                    },
                                    onBackspaceHold = onBackspaceHold,
                                    onBackspaceRelease = onBackspaceRelease
                                )

                                KeyboardRow(
                                    keys = KeyboardLayouts.SYMBOLS_BOTTOM_ROW,
                                    rowIdx = 3,
                                    keyHeight = baseKeyHeight,
                                    shape = shape,
                                    theme = theme,
                                    config = config,
                                    activeWaves = activeWaves,
                                    ambientPhase = ambientPhase,
                                    scale = scale,
                                    isShifted = false,
                                    keyGap = keyGap,
                                    waveTick = waveTick,
                                    onKeyClick = { key ->
                                        when (key.type) {
                                            KeyType.SWITCH_LETTERS -> layoutMode = KeyboardLayoutMode.LETTERS
                                            KeyType.EMOJI -> layoutMode = KeyboardLayoutMode.EMOJIS
                                            else -> onKeyPress(key)
                                        }
                                    }
                                )
                            }
                        }

                        KeyboardLayoutMode.EMOJIS -> {
                            // Dedicated Emoji Keyboard with categorized navigation (Requirement 12)
                            EmojiPanel(
                                baseKeyHeight = baseKeyHeight,
                                subpanelHeight = subpanelHeight,
                                scale = scale,
                                theme = theme,
                                shape = shape,
                                config = config,
                                onEmojiClick = { emoji ->
                                    triggerWave(2, 4, theme.accentColor, activeWaves, config)
                                    onKeyPress(KeyItem(emoji))
                                },
                                onBackspace = {
                                    onKeyPress(KeyItem("", type = KeyType.BACKSPACE))
                                },
                                onBackspaceHold = onBackspaceHold,
                                onBackspaceRelease = onBackspaceRelease,
                                onBackToLetters = {
                                    layoutMode = KeyboardLayoutMode.LETTERS
                                },
                                onSwitchSymbols = {
                                    layoutMode = KeyboardLayoutMode.SYMBOLS
                                }
                            )
                        }
                    }
                }
            }

            // Wrapping keyboardContent in One-Handed rail or responsive width
            if (config.oneHandedMode == OneHandedMode.OFF) {
                val widthFraction = (config.keyboardWidthPercent.coerceIn(75, 100) / 100f)
                Box(
                    modifier = if (widthFraction < 1f) Modifier.fillMaxWidth(widthFraction).align(Alignment.CenterHorizontally) else Modifier.fillMaxWidth()
                ) {
                    keyboardContent()
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (config.oneHandedMode == OneHandedMode.RIGHT) Arrangement.End else Arrangement.Start,
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (config.oneHandedMode == OneHandedMode.RIGHT) {
                        OneHandedSideRail(
                            theme = theme,
                            scale = scale,
                            onExpand = { onUpdateConfig { it.copy(oneHandedMode = OneHandedMode.OFF) } },
                            onFlip = { onUpdateConfig { it.copy(oneHandedMode = OneHandedMode.LEFT) } },
                            onResize = { activeSubpanel = ActiveSubpanel.RESIZE },
                            modifier = Modifier
                                .width(44.dp)
                                .height(subpanelHeight)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        keyboardContent()
                    }

                    if (config.oneHandedMode == OneHandedMode.LEFT) {
                        Spacer(modifier = Modifier.width(4.dp))
                        OneHandedSideRail(
                            theme = theme,
                            scale = scale,
                            onExpand = { onUpdateConfig { it.copy(oneHandedMode = OneHandedMode.OFF) } },
                            onFlip = { onUpdateConfig { it.copy(oneHandedMode = OneHandedMode.RIGHT) } },
                            onResize = { activeSubpanel = ActiveSubpanel.RESIZE },
                            modifier = Modifier
                                .width(44.dp)
                                .height(subpanelHeight)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OneHandedSideRail(
    theme: KeyboardTheme,
    scale: Float,
    onExpand: () -> Unit,
    onFlip: () -> Unit,
    onResize: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(theme.keyColor).copy(alpha = 0.55f))
            .border(1.dp, Color(theme.borderColor).copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Expand
        Box(
            modifier = Modifier
                .size((34 * scale).coerceIn(30f, 40f).dp)
                .clip(CircleShape)
                .background(Color(theme.specialKeyColor))
                .clickable { onExpand() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.OpenInFull,
                contentDescription = "Full Width",
                tint = Color(theme.textColor),
                modifier = Modifier.size(17.dp)
            )
        }

        // Flip side
        Box(
            modifier = Modifier
                .size((34 * scale).coerceIn(30f, 40f).dp)
                .clip(CircleShape)
                .background(Color(theme.specialKeyColor))
                .clickable { onFlip() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Switch Side",
                tint = Color(theme.accentColor),
                modifier = Modifier.size(17.dp)
            )
        }

        // Resize / Tools
        Box(
            modifier = Modifier
                .size((34 * scale).coerceIn(30f, 40f).dp)
                .clip(CircleShape)
                .background(Color(theme.specialKeyColor))
                .clickable { onResize() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Resize",
                tint = Color(theme.textColor),
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

/**
 * Suggestion Bar with local word completions and sleek typography.
 */
@Composable
private fun SuggestionBar(
    suggestions: List<String>,
    theme: KeyboardTheme,
    config: KeyboardConfig,
    onSuggestionClick: (String) -> Unit
) {
    val density = LocalDensity.current
    val safeFontScale = density.fontScale.coerceIn(0.85f, 1.25f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp * config.scaleFactor)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(theme.specialKeyColor).copy(alpha = if (config.glassMode) 0.35f else 0.65f))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (suggestions.isEmpty()) {
            Text(
                text = "KeyWave",
                fontSize = (12f / safeFontScale).coerceAtLeast(10f).sp,
                color = Color(theme.specialTextColor).copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 12.dp),
                style = LocalTextStyle.current.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
        } else {
            suggestions.take(4).forEachIndexed { index, word ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onSuggestionClick(word) }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = word,
                        fontSize = ((13f * config.scaleFactor) / safeFontScale).coerceIn(10f, 15f).sp,
                        fontWeight = if (index == 0) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (index == 0) Color(theme.accentColor) else Color(theme.textColor),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        style = LocalTextStyle.current.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }
                if (index < suggestions.take(4).size - 1) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(14.dp)
                            .background(Color(theme.borderColor).copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}

/**
 * Dedicated Emoji Panel supporting categories, backspace and returning to letters.
 */
@Composable
private fun EmojiPanel(
    baseKeyHeight: Dp,
    subpanelHeight: Dp,
    scale: Float,
    theme: KeyboardTheme,
    shape: Shape,
    config: KeyboardConfig,
    onEmojiClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onBackspaceHold: () -> Unit = {},
    onBackspaceRelease: () -> Unit = {},
    onBackToLetters: () -> Unit,
    onSwitchSymbols: () -> Unit = {}
) {
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val categories = KeyboardLayouts.EMOJI_CATEGORIES
    val currentCategory = categories[selectedCategoryIndex]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(subpanelHeight)
    ) {
        // Category tabs row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(categories.indices.toList()) { idx ->
                val cat = categories[idx]
                val isSelected = idx == selectedCategoryIndex
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) Color(theme.accentColor).copy(alpha = 0.25f)
                            else Color.Transparent
                        )
                        .clickable { selectedCategoryIndex = idx }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${cat.icon} ${cat.name}",
                        fontSize = 11.sp * scale,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(theme.accentColor) else Color(theme.specialTextColor)
                    )
                }
            }
        }

        // Emoji Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(currentCategory.emojis) { emoji ->
                var isEmojiPressed by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .size(38.dp * scale)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isEmojiPressed) Color(theme.accentColor).copy(alpha = 0.35f)
                            else Color.Transparent
                        )
                        .pointerInput(emoji) {
                            detectTapGestures(
                                onPress = {
                                    isEmojiPressed = true
                                    onEmojiClick(emoji)
                                    tryAwaitRelease()
                                    isEmojiPressed = false
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = 20.sp * scale,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Bottom action bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(shape)
                        .background(Color(theme.specialKeyColor))
                        .clickable { onBackToLetters() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "ABC",
                        fontSize = 13.sp * scale,
                        fontWeight = FontWeight.Bold,
                        color = Color(theme.textColor)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(shape)
                        .background(Color(theme.specialKeyColor))
                        .clickable { onSwitchSymbols() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "?123",
                        fontSize = 13.sp * scale,
                        fontWeight = FontWeight.Bold,
                        color = Color(theme.textColor)
                    )
                }
            }

            Text(
                text = currentCategory.name,
                fontSize = 11.sp * scale,
                color = Color(theme.specialTextColor)
            )

            Box(
                modifier = Modifier
                    .clip(shape)
                    .background(Color(theme.specialKeyColor))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                onBackspace()
                                try {
                                    tryAwaitRelease()
                                } finally {
                                    onBackspaceRelease()
                                }
                            },
                            onLongPress = {
                                onBackspaceHold()
                            }
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "⌫",
                    fontSize = 14.sp * scale,
                    color = Color(theme.textColor)
                )
            }
        }
    }
}

private fun triggerWave(
    row: Int,
    col: Int,
    waves: MutableList<ActiveWave>,
    config: KeyboardConfig
) {
    if (config.performanceMode || config.waveOpacity <= 0.001f) return
    val now = SystemClock.uptimeMillis()
    waves.removeAll { now - it.timestamp > 850L }
    if (waves.size < 8) {
        waves.add(ActiveWave(row, col, now))
    }
}

// Overload supporting callers with color, ensuring the single global wave color is always used
private fun triggerWave(
    row: Int,
    col: Int,
    @Suppress("UNUSED_PARAMETER") color: Long,
    waves: MutableList<ActiveWave>,
    config: KeyboardConfig
) {
    triggerWave(row, col, waves, config)
}

@Composable
private fun KeyboardRow(
    keys: List<KeyItem>,
    rowIdx: Int,
    keyHeight: Dp,
    shape: Shape,
    theme: KeyboardTheme,
    config: KeyboardConfig,
    activeWaves: MutableList<ActiveWave>,
    ambientPhase: Float,
    scale: Float,
    isShifted: Boolean,
    sideMarginPercent: Float = 0.0f,
    keyGap: Dp = 4.dp * scale,
    waveTick: Long = 0L,
    onKeyClick: (KeyItem) -> Unit,
    onBackspaceHold: () -> Unit = {},
    onBackspaceRelease: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(keyHeight),
        horizontalArrangement = Arrangement.spacedBy(keyGap)
    ) {
        if (sideMarginPercent > 0.001f) {
            Spacer(modifier = Modifier.weight(sideMarginPercent))
        }

        keys.forEachIndexed { colIdx, key ->
            val indexedKey = remember(key, rowIdx, colIdx) {
                key.copy(row = rowIdx, col = colIdx)
            }
            KeyCell(
                key = indexedKey,
                keyHeight = keyHeight,
                shape = shape,
                theme = theme,
                config = config,
                activeWaves = activeWaves,
                waveTick = waveTick,
                ambientPhase = ambientPhase,
                scale = scale,
                isShifted = isShifted,
                onClick = {
                    triggerWave(rowIdx, colIdx, activeWaves, config)
                    onKeyClick(indexedKey)
                },
                onHold = if (key.type == KeyType.BACKSPACE) onBackspaceHold else null,
                onHoldRelease = if (key.type == KeyType.BACKSPACE) onBackspaceRelease else null,
                modifier = Modifier.weight(key.weight)
            )
        }

        if (sideMarginPercent > 0.001f) {
            Spacer(modifier = Modifier.weight(sideMarginPercent))
        }
    }
}

@Composable
private fun KeyCell(
    key: KeyItem,
    keyHeight: Dp,
    shape: Shape,
    theme: KeyboardTheme,
    config: KeyboardConfig,
    activeWaves: List<ActiveWave>,
    waveTick: Long = 0L,
    ambientPhase: Float,
    scale: Float,
    isShifted: Boolean,
    onClick: () -> Unit,
    onHold: (() -> Unit)? = null,
    onHoldRelease: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    DisposableEffect(key) {
        onDispose {
            if (key.type == KeyType.BACKSPACE) {
                onHoldRelease?.invoke()
            }
        }
    }

    var isPressed by remember { mutableStateOf(false) }
    val pressScale = remember { Animatable(1.0f) }
    val coroutineScope = rememberCoroutineScope()

    // Smooth wave ripple propagation across keys (Requirement 2 & 13)
    val waveIntensity = remember(
        waveTick,
        activeWaves.size,
        config.waveSpeed,
        key.row,
        key.col
    ) {
        if (activeWaves.isEmpty() || waveTick == 0L || config.waveOpacity <= 0.001f) {
            0f
        } else {
            val now = waveTick
            var maxWave = 0f
            val speedFactor = config.waveSpeed.speedFactor
            for (w in activeWaves) {
                val elapsed = (now - w.timestamp).toFloat()
                if (elapsed < 0f || elapsed > 850f) continue

                val dRow = (key.row - w.originRow).toFloat()
                val dCol = (key.col - w.originCol).toFloat()
                val dist = sqrt(dRow * dRow + dCol * dCol)

                if (dist < 0.6f) {
                    val fade = (1.0f - (elapsed / 420f)).coerceIn(0f, 1f)
                    val hit = fade * fade
                    if (hit > maxWave) maxWave = hit
                } else {
                    val waveDist = elapsed * 0.011f * speedFactor
                    val delta = kotlin.math.abs(dist - waveDist)
                    if (delta < 1.6f) {
                        val crest = 1.0f - (delta / 1.6f)
                        val timeDecay = (1.0f - (elapsed / 850f)).coerceIn(0f, 1f)
                        val distDecay = (1.0f - (dist / 7.5f)).coerceIn(0f, 1f)
                        val hit = crest * timeDecay * distDecay
                        if (hit > maxWave) maxWave = hit
                    }
                }
            }
            maxWave
        }
    }

    // Effective intensity: on press down, immediately maximize
    val effectiveWaveIntensity = if (isPressed) maxOf(waveIntensity, 0.95f) else waveIntensity
    // Wave alpha strictly respects user's selected waveOpacity (Requirement 7 & 19)
    val waveAlpha = (effectiveWaveIntensity * config.waveOpacity).coerceIn(0f, 1f)

    // Global Wave Color: ALWAYS uses the single global wave color or moving RGB rainbow (Requirements 3, 4, 5, 18)
    val waveColor = remember(
        config.waveColorMode,
        config.globalWaveColor,
        ambientPhase,
        key.row,
        key.col
    ) {
        when (config.waveColorMode) {
            WaveColorMode.RAINBOW -> {
                val hue = (ambientPhase * 360f + (key.col * 24f) + (key.row * 36f)) % 360f
                hsvToColor(hue, 0.90f, 1.0f)
            }
            WaveColorMode.SOLID -> {
                Color(config.globalWaveColor)
            }
        }
    }

    // Key base colors: keeps the normal keyboard appearance (Requirements 1 & 14)
    val isSpecialKey = key.type != KeyType.CHARACTER
    val baseKeyColor = remember(theme, isSpecialKey, config.glassMode, config.minimalisticMode) {
        when {
            config.glassMode -> {
                if (isSpecialKey) Color(theme.specialKeyColor).copy(alpha = 0.35f)
                else Color(theme.keyColor).copy(alpha = 0.28f)
            }
            else -> {
                if (isSpecialKey) Color(theme.specialKeyColor)
                else Color(theme.keyColor)
            }
        }
    }

    val finalKeyColor = remember(baseKeyColor, isPressed, theme) {
        if (isPressed) Color(theme.keyActiveColor) else baseKeyColor
    }

    val finalTextColor = remember(theme, isSpecialKey, isPressed) {
        when {
            isPressed -> Color.White
            isSpecialKey -> Color(theme.specialTextColor)
            else -> Color(theme.textColor)
        }
    }

    val secondaryTextColor = remember(theme) {
        Color(theme.specialTextColor).copy(alpha = 0.55f)
    }

    // Border: Glows with the global wave color during wave ripple, otherwise normal theme border
    val borderColor = remember(theme, isPressed, waveAlpha, waveColor, config.glassMode) {
        when {
            waveAlpha > 0.08f -> waveColor.copy(alpha = (waveAlpha * 1.15f).coerceIn(0.25f, 1f))
            config.glassMode -> Color(theme.borderColor).copy(alpha = 0.35f)
            else -> Color(theme.borderColor).copy(alpha = 0.40f)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .scale(pressScale.value)
            .clip(shape) // STRICT CLIPPING: animations/glow/wave strictly clipped to key shape (Requirements 11 & 12)
            .background(finalKeyColor)
            .border(
                width = if (config.glassMode || waveAlpha > 0.1f) 1.dp else 0.5.dp,
                color = borderColor,
                shape = shape
            )
            .drawBehind {
                // Smooth radial wave overlay clipped strictly inside key geometry (Requirements 9, 10, 11, 12)
                if (waveAlpha > 0.005f) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                waveColor.copy(alpha = waveAlpha * 0.90f),
                                waveColor.copy(alpha = waveAlpha * 0.40f),
                                Color.Transparent
                            ),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = maxOf(size.width, size.height) * 1.25f
                        )
                    )
                }
            }
            .pointerInput(key) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onClick()
                        coroutineScope.launch {
                            pressScale.animateTo(
                                targetValue = 0.93f,
                                animationSpec = tween(35)
                            )
                        }
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                            if (key.type == KeyType.BACKSPACE) {
                                onHoldRelease?.invoke()
                            }
                            coroutineScope.launch {
                                pressScale.animateTo(
                                    targetValue = 1.0f,
                                    animationSpec = tween(80)
                                )
                            }
                        }
                    },
                    onLongPress = {
                        if (onHold != null) {
                            onHold()
                        }
                    }
                )
            }
            .testTag("key_${key.primaryChar}"),
        contentAlignment = Alignment.Center
    ) {
        val availableHeight = maxHeight
        val availableWidth = maxWidth
        val density = LocalDensity.current

        // Safe density normalization for user accessibility font scaling:
        // Prevents keys from overflowing or colliding on large display/font scale settings
        val systemFontScale = density.fontScale.coerceAtLeast(1.0f)
        val safeFontScale = systemFontScale.coerceIn(0.85f, 1.25f)

        // Robust measurement logic for character padding that respects screen bounds
        val horizPadding = (availableWidth * 0.07f).coerceIn(2.dp, 5.dp)
        val vertPadding = (availableHeight * 0.05f).coerceIn(1.5.dp, 4.dp)

        val label = key.displayLabel ?: if (isShifted) key.shiftChar else key.primaryChar
        val hasSecondary = key.secondaryChar != null && !isSpecialKey && key.type == KeyType.CHARACTER

        if (hasSecondary) {
            // DISTINCT, NON-OVERLAPPING CONSTRAINTS:
            // 1. Secondary Symbol Zone: strictly allocated to the top slice (height = secZoneHeight)
            // 2. Primary Label Zone: strictly allocated to the remaining bottom slice (weight = 1f)
            // The two zones have zero vertical intersection, avoiding any overlap or clipping.
            val secZoneHeight = (availableHeight * 0.30f).coerceIn(10.dp, 15.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizPadding, vertical = vertPadding),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Secondary Symbol Region: strictly top-right constrained
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(secZoneHeight),
                    contentAlignment = Alignment.TopEnd
                ) {
                    val secFontSize = ((secZoneHeight.value * 0.65f) / safeFontScale).coerceIn(7.5f, 10.5f).sp
                    Text(
                        text = key.secondaryChar ?: "",
                        color = secondaryTextColor,
                        fontSize = secFontSize,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        style = LocalTextStyle.current.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }

                // Primary Character Region: strictly centered in the remaining bottom space
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    val usableWidth = (availableWidth - horizPadding * 2).value
                    val usableHeight = (availableHeight - secZoneHeight - vertPadding * 2).value
                    val primFontSize = minOf(
                        16.5f * scale,
                        usableWidth * 0.65f,
                        usableHeight * 0.60f
                    ) / safeFontScale

                    Text(
                        text = label,
                        color = finalTextColor,
                        fontSize = primFontSize.coerceIn(10f, 19f).sp,
                        fontWeight = if (isPressed) FontWeight.SemiBold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        style = LocalTextStyle.current.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }
            }
        } else {
            // Single primary character or multi-character label (e.g. ?123, =\<, ABC, KeyWave, Shift, Backspace, Enter)
            val usableWidth = (availableWidth - horizPadding * 2).value
            val usableHeight = (availableHeight - vertPadding * 2).value
            val charCount = label.length

            val baseFontSize = when {
                key.type == KeyType.SPACE -> 12f
                key.type == KeyType.SWITCH_SYMBOLS || key.type == KeyType.SWITCH_LETTERS -> 12f
                key.type == KeyType.SWITCH_EXTRA_SYMBOLS -> 11f
                key.type == KeyType.EMOJI -> 14f
                key.type == KeyType.SHIFT || key.type == KeyType.BACKSPACE || key.type == KeyType.ENTER -> 15f
                else -> 18f
            } * scale

            val maxFromWidth = if (charCount > 1) {
                (usableWidth / (charCount * 0.60f)) / safeFontScale
            } else {
                (usableWidth * 0.70f) / safeFontScale
            }
            val maxFromHeight = (usableHeight * 0.52f) / safeFontScale

            val primaryFontSize = minOf(
                baseFontSize / safeFontScale,
                maxFromWidth,
                maxFromHeight
            ).coerceIn(8f, 22f).sp

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizPadding, vertical = vertPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = finalTextColor,
                    fontSize = primaryFontSize,
                    fontWeight = if (isSpecialKey || isPressed) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    style = LocalTextStyle.current.copy(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
            }
        }
    }
}

private fun computeRGBColor(
    mode: RGBMode,
    ambientPhase: Float,
    waveIntensity: Float,
    row: Int,
    col: Int,
    theme: KeyboardTheme,
    brightness: Float
): Color {
    if (mode == RGBMode.OFF) return Color.Transparent

    return when (mode) {
        RGBMode.OFF -> Color.Transparent

        RGBMode.STATIC -> {
            Color(theme.accentColor).copy(alpha = brightness * 0.7f)
        }

        RGBMode.MINIMAL_GLOW -> {
            Color(theme.glowColor).copy(alpha = brightness * 0.45f)
        }

        RGBMode.REACTIVE, RGBMode.RGB_WAVE -> {
            if (waveIntensity > 0.05f) {
                Color(theme.accentColor).copy(alpha = (waveIntensity * brightness).coerceIn(0f, 1f))
            } else {
                Color.Transparent
            }
        }

        RGBMode.RAINBOW -> {
            val hue = (ambientPhase * 360f + (col * 24f) + (row * 36f)) % 360f
            hsvToColor(hue, 0.85f, brightness)
        }

        RGBMode.AURORA -> {
            val offset = (ambientPhase + (col * 0.08f) + (row * 0.12f)) % 1f
            val green = Color(0xFF52B788)
            val teal = Color(0xFF00F5D4)
            val purple = Color(0xFF7B2CBF)
            val blended = when {
                offset < 0.5f -> lerpColor(green, teal, offset * 2f)
                else -> lerpColor(teal, purple, (offset - 0.5f) * 2f)
            }
            blended.copy(alpha = brightness * 0.8f)
        }

        RGBMode.BREATHING -> {
            val breath = (sin(ambientPhase * 2f * Math.PI.toFloat()) + 1f) / 2f
            Color(theme.accentColor).copy(alpha = (breath * brightness * 0.85f).coerceIn(0.1f, 1f))
        }

        RGBMode.PULSE -> {
            val pulse = (sin((ambientPhase * 4f + col * 0.2f) * Math.PI.toFloat()) + 1f) / 2f
            Color(theme.secondaryColor).copy(alpha = (pulse * brightness * 0.85f).coerceIn(0.1f, 1f))
        }

        RGBMode.NEON -> {
            val c = if (col % 2 == 0) Color(0xFF00F0FF) else Color(0xFFFF007F)
            c.copy(alpha = brightness * 0.75f)
        }

        RGBMode.FIRE -> {
            val flicker = (sin((ambientPhase * 6f + col * 0.5f + row) * Math.PI.toFloat()) + 1f) / 2f
            val orange = Color(0xFFFF5400)
            val yellow = Color(0xFFFFB703)
            lerpColor(orange, yellow, flicker).copy(alpha = brightness * 0.8f)
        }

        RGBMode.OCEAN -> {
            val wave = (sin((ambientPhase * 2.5f + col * 0.35f + row * 0.5f) * Math.PI.toFloat()) + 1f) / 2f
            val deep = Color(0xFF0077B6)
            val aqua = Color(0xFF00F5D4)
            lerpColor(deep, aqua, wave).copy(alpha = brightness * 0.8f)
        }
    }
}

private fun lerpColor(c1: Color, c2: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = c1.red + (c2.red - c1.red) * f,
        green = c1.green + (c2.green - c1.green) * f,
        blue = c1.blue + (c2.blue - c1.blue) * f,
        alpha = c1.alpha + (c2.alpha - c1.alpha) * f
    )
}

private fun hsvToColor(hue: Float, saturation: Float, value: Float): Color {
    val h = (hue % 360f) / 60f
    val c = value * saturation
    val x = c * (1f - kotlin.math.abs(h % 2f - 1f))
    val m = value - c
    val (r, g, b) = when {
        h < 1f -> Triple(c, x, 0f)
        h < 2f -> Triple(x, c, 0f)
        h < 3f -> Triple(0f, c, x)
        h < 4f -> Triple(0f, x, c)
        h < 5f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color(r + m, g + m, b + m, 1.0f)
}
