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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.audio.KeyWaveSoundManager
import com.example.data.KeyboardPreferences
import com.example.haptics.KeyWaveHapticManager
import com.example.keyboard.KeyboardLayoutView
import com.example.keyboard.tools.UndoRedoManager
import android.content.ClipData
import android.content.ClipboardManager
import com.example.model.KeyItem
import com.example.model.KeyType
import com.example.model.KeyboardConfig
import com.example.model.WordDictionary
import com.example.ui.components.GlassCard

@Composable
fun HomeScreen(
    config: KeyboardConfig,
    preferences: KeyboardPreferences,
    onNavigateToCustomize: () -> Unit,
    onNavigateToThemes: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToOnboarding: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val hapticManager = remember { KeyWaveHapticManager(context) }
    val soundManager = remember { KeyWaveSoundManager(context) }
    DisposableEffect(soundManager) {
        onDispose {
            soundManager.release()
        }
    }

    var isImeEnabled by remember { mutableStateOf(false) }
    var isImeSelected by remember { mutableStateOf(false) }
    var testInputText by remember { mutableStateOf("") }
    var currentToken by remember { mutableStateOf("") }
    var isInputActive by remember { mutableStateOf(false) }
    val undoRedoManager = remember { UndoRedoManager() }
    val canUndo by undoRedoManager.canUndo.collectAsState()
    val canRedo by undoRedoManager.canRedo.collectAsState()

    val homeScope = rememberCoroutineScope()
    var backspaceRepeatJob by remember { mutableStateOf<Job?>(null) }

    fun stopBackspaceRepeat() {
        backspaceRepeatJob?.cancel()
        backspaceRepeatJob = null
        hapticManager.cancel()
    }

    fun startContinuousBackspace() {
        stopBackspaceRepeat()
        backspaceRepeatJob = homeScope.launch {
            delay(180)
            while (isActive) {
                if (testInputText.isNotEmpty()) {
                    testInputText = if (testInputText.length >= 2 && Character.isSurrogatePair(testInputText[testInputText.length - 2], testInputText[testInputText.length - 1])) {
                        testInputText.dropLast(2)
                    } else {
                        testInputText.dropLast(1)
                    }
                    undoRedoManager.pushState(testInputText)
                }
                if (currentToken.isNotEmpty()) currentToken = currentToken.dropLast(1)
                if (config.vibrateOnKeypress) hapticManager.vibrateKeypress(config.hapticStrength)
                delay(55)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            stopBackspaceRepeat()
        }
    }

    fun checkImeStatus() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val enabledList = imm?.enabledInputMethodList ?: emptyList()
        isImeEnabled = enabledList.any { it.packageName == context.packageName }

        val currentIme = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        )
        isImeSelected = currentIme != null && currentIme.contains(context.packageName)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkImeStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        checkImeStatus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A10))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "KeyWave",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Clean, Minimal & Modern Keyboard",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF38BDF8)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x2238BDF8))
                    .border(1.dp, Color(0x4438BDF8), RoundedCornerShape(12.dp))
                    .clickable { onNavigateToOnboarding() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Setup IME",
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Test Typing Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0x25141829))
                .border(1.dp, Color(0x334E5D8F), RoundedCornerShape(14.dp))
                .clickable {
                    if (!isInputActive) {
                        testInputText = ""
                        currentToken = ""
                        isInputActive = true
                    }
                }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (testInputText.isEmpty()) "Tap keyboard below to test typing..." else testInputText,
                    fontSize = 16.sp,
                    color = if (testInputText.isEmpty()) Color(0xFF64748B) else Color(0xFFF1F5F9),
                    modifier = Modifier.weight(1f)
                )
                if (testInputText.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                            .clickable {
                                testInputText = ""
                                currentToken = ""
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Clear", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Large Live Keyboard Preview
        GlassCard(
            backgroundColor = Color(0x18182035),
            borderColor = Color(0x284E5D8F)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LIVE KEYBOARD PREVIEW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Interactive",
                    fontSize = 11.sp,
                    color = Color(0xFF38BDF8)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            KeyboardLayoutView(
                config = config,
                onKeyPress = { key ->
                    if (key.type != KeyType.BACKSPACE) {
                        stopBackspaceRepeat()
                    }

                    if (config.vibrateOnKeypress) {
                        hapticManager.vibrateKeypress(config.hapticStrength)
                    }
                    if (config.soundOnKeypress) {
                        soundManager.playKeySound(config.soundStyle, config.soundVolume)
                    }

                    when (key.type) {
                        KeyType.BACKSPACE -> {
                            if (testInputText.isNotEmpty()) {
                                testInputText = if (testInputText.length >= 2 && Character.isSurrogatePair(testInputText[testInputText.length - 2], testInputText[testInputText.length - 1])) {
                                    testInputText.dropLast(2)
                                } else {
                                    testInputText.dropLast(1)
                                }
                                undoRedoManager.pushState(testInputText)
                            }
                            if (currentToken.isNotEmpty()) {
                                currentToken = currentToken.dropLast(1)
                            }
                        }
                        KeyType.SPACE -> {
                            if (config.autoCorrection && currentToken.length >= 3) {
                                val corr = WordDictionary.getAutoCorrection(currentToken)
                                if (corr != null) {
                                    testInputText = testInputText.dropLast(currentToken.length) + corr
                                }
                            }
                            testInputText += " "
                            undoRedoManager.pushState(testInputText)
                            currentToken = ""
                        }
                        KeyType.ENTER -> {
                            testInputText += "\n"
                            undoRedoManager.pushState(testInputText)
                            currentToken = ""
                        }
                        else -> {
                            val char = key.primaryChar
                            testInputText += char
                            undoRedoManager.pushState(testInputText)
                            if (char.length == 1 && char[0].isLetter()) {
                                currentToken += char
                            } else {
                                currentToken = ""
                            }
                        }
                    }
                },
                onBackspaceHold = {
                    startContinuousBackspace()
                },
                onBackspaceRelease = {
                    stopBackspaceRepeat()
                },
                currentWordPrefix = currentToken,
                onSuggestionSelected = { word ->
                    if (currentToken.isNotEmpty()) {
                        testInputText = testInputText.dropLast(currentToken.length)
                    }
                    testInputText += "$word "
                    undoRedoManager.pushState(testInputText)
                    currentToken = ""
                    if (config.vibrateOnKeypress) hapticManager.vibrateKeypress(config.hapticStrength)
                },
                canUndo = canUndo,
                canRedo = canRedo,
                onUndo = {
                    undoRedoManager.undo()?.let {
                        testInputText = it
                        currentToken = ""
                    }
                },
                onRedo = {
                    undoRedoManager.redo()?.let {
                        testInputText = it
                        currentToken = ""
                    }
                },
                onUpdateConfig = { update ->
                    preferences.updateConfig { update(it) }
                },
                onNavigateToCustomize = onNavigateToCustomize,
                onNavigateToSettings = onNavigateToSettings,
                onSelectAll = {},
                onCopy = {
                    if (testInputText.isNotEmpty()) {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        clipboard?.setPrimaryClip(ClipData.newPlainText("KeyWave", testInputText))
                    }
                },
                onCut = {
                    if (testInputText.isNotEmpty()) {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        clipboard?.setPrimaryClip(ClipData.newPlainText("KeyWave", testInputText))
                        testInputText = ""
                        currentToken = ""
                        undoRedoManager.pushState("")
                    }
                },
                onPaste = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    val item = clipboard?.primaryClip?.getItemAt(0)
                    item?.text?.let { pasteText ->
                        testInputText += pasteText
                        undoRedoManager.pushState(testInputText)
                    }
                },
                onMoveCursorLeft = {},
                onMoveCursorRight = {},
                onMoveCursorStart = {},
                onMoveCursorEnd = {}
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Quick Action Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionButton(
                label = "Customize",
                icon = Icons.Default.Tune,
                gradient = listOf(Color(0xFF38BDF8), Color(0xFF818CF8)),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToCustomize
            )
            QuickActionButton(
                label = "Themes",
                icon = Icons.Default.Palette,
                gradient = listOf(Color(0xFFC084FC), Color(0xFF38BDF8)),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToThemes
            )
            QuickActionButton(
                label = "Settings",
                icon = Icons.Default.Settings,
                gradient = listOf(Color(0xFF64748B), Color(0xFF334155)),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToSettings
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // IME Status Card
        GlassCard {
            Text(
                text = "System Activation Status",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))

            StatusStepItem(
                step = 1,
                title = "Enable KeyWave in Android Settings",
                isCompleted = isImeEnabled,
                actionLabel = "Enable",
                onAction = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                    } catch (_: Exception) {}
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            StatusStepItem(
                step = 2,
                title = "Select KeyWave as Active Keyboard",
                isCompleted = isImeSelected,
                actionLabel = "Select",
                onAction = {
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.showInputMethodPicker()
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(gradient))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun StatusStepItem(
    step: Int,
    title: String,
    isCompleted: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isCompleted) Color(0x1810B981) else Color(0x18F59E0B))
            .border(
                1.dp,
                if (isCompleted) Color(0x3310B981) else Color(0x33F59E0B),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isCompleted) Color(0xFF10B981) else Color(0xFFF59E0B),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Step $step: $title",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE2E8F0)
            )
        }
        if (!isCompleted) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF38BDF8))
                    .clickable { onAction() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = actionLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF090A10)
                )
            }
        }
    }
}
