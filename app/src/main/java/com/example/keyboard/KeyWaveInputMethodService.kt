package com.example.keyboard

import android.inputmethodservice.InputMethodService
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import com.example.MainActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.audio.KeyWaveSoundManager
import com.example.data.KeyboardPreferences
import com.example.haptics.KeyWaveHapticManager
import com.example.model.KeyItem
import com.example.model.KeyType
import com.example.model.WordDictionary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Architecture hook for future gesture typing without interrupting normal typing.
 */
interface GestureTypingListener {
    fun onGestureStart(x: Float, y: Float)
    fun onGestureMove(x: Float, y: Float)
    fun onGestureEnd(word: String?)
}

class KeyWaveInputMethodService : InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val mViewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = mViewModelStore

    private lateinit var preferences: KeyboardPreferences
    private lateinit var hapticManager: KeyWaveHapticManager
    private lateinit var soundManager: KeyWaveSoundManager
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Lifecycle-safe Backspace repeat state and cancellation
    private val backspaceLock = Any()
    @Volatile
    private var isBackspaceRepeating = false
    private var backspaceRepeatJob: Job? = null
    private val backspaceHandler = Handler(Looper.getMainLooper())
    private var backspaceRunnable: Runnable? = null

    // Tracking current word token for suggestion bar and auto-correction
    private var currentWordToken by mutableStateOf("")

    // Gesture typing integration point
    var gestureListener: GestureTypingListener? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        preferences = KeyboardPreferences.getInstance(this)
        hapticManager = KeyWaveHapticManager(this)
        soundManager = KeyWaveSoundManager(this)
    }

    override fun onCreateInputView(): View {
        val composeView = ComposeView(this).apply {
            // Attach view tree owners so Compose can run safely inside InputMethodService
            window?.window?.decorView?.let { decor ->
                decor.setViewTreeLifecycleOwner(this@KeyWaveInputMethodService)
                decor.setViewTreeViewModelStoreOwner(this@KeyWaveInputMethodService)
                decor.setViewTreeSavedStateRegistryOwner(this@KeyWaveInputMethodService)
            }
            setViewTreeLifecycleOwner(this@KeyWaveInputMethodService)
            setViewTreeViewModelStoreOwner(this@KeyWaveInputMethodService)
            setViewTreeSavedStateRegistryOwner(this@KeyWaveInputMethodService)

            setContent {
                val config by preferences.configFlow.collectAsState()

                KeyboardLayoutView(
                    config = config,
                    onKeyPress = { key -> handleKeyInput(key) },
                    onBackspaceHold = { startContinuousBackspace() },
                    onBackspaceRelease = { stopBackspaceRepeat() },
                    currentWordPrefix = currentWordToken,
                    onSuggestionSelected = { word -> applySuggestion(word) },
                    onUpdateConfig = { update ->
                        preferences.updateConfig { update(it) }
                    },
                    onNavigateToCustomize = { launchApp("customize") },
                    onNavigateToSettings = { launchApp("settings") },
                    onSelectAll = {
                        currentInputConnection?.performContextMenuAction(android.R.id.selectAll)
                    },
                    onCopy = {
                        currentInputConnection?.performContextMenuAction(android.R.id.copy)
                    },
                    onCut = {
                        currentInputConnection?.performContextMenuAction(android.R.id.cut)
                    },
                    onPaste = {
                        currentInputConnection?.performContextMenuAction(android.R.id.paste)
                    },
                    onUndo = {
                        currentInputConnection?.performContextMenuAction(android.R.id.undo)
                    },
                    onRedo = {
                        currentInputConnection?.performContextMenuAction(android.R.id.redo)
                    },
                    onMoveCursorLeft = {
                        sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT)
                    },
                    onMoveCursorRight = {
                        sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT)
                    },
                    onMoveCursorStart = {
                        currentInputConnection?.setSelection(0, 0)
                    },
                    onMoveCursorEnd = {
                        val extracted = currentInputConnection?.getExtractedText(ExtractedTextRequest(), 0)
                        val len = extracted?.text?.length ?: 10000
                        currentInputConnection?.setSelection(len, len)
                    }
                )
            }
        }

        return composeView
    }

    private fun launchApp(destination: String? = null) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            if (destination != null) {
                putExtra("destination", destination)
            }
        }
        startActivity(intent)
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        stopBackspaceRepeat()
        currentWordToken = ""
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        stopBackspaceRepeat()
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        stopBackspaceRepeat()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        stopBackspaceRepeat()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        currentWordToken = ""
        stopBackspaceRepeat()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBackspaceRepeat()
        serviceScope.cancel()
        soundManager.release()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        viewModelStore.clear()
    }

    private fun handleKeyInput(key: KeyItem) {
        if (key.type != KeyType.BACKSPACE) {
            stopBackspaceRepeat()
        }

        val config = preferences.configFlow.value
        val ic = currentInputConnection ?: return

        // 1. Trigger haptic & sound immediately with zero delay
        if (config.vibrateOnKeypress) {
            hapticManager.vibrateKeypress(config.hapticStrength)
        }
        if (config.soundOnKeypress) {
            soundManager.playKeySound(config.soundStyle, config.soundVolume)
        }

        // 2. Commit input based on key type
        when (key.type) {
            KeyType.CHARACTER -> {
                val char = key.primaryChar
                ic.commitText(char, 1)
                if (char.length == 1 && char[0].isLetter()) {
                    currentWordToken += char
                } else {
                    currentWordToken = ""
                }
            }
            KeyType.SPACE -> {
                // Auto-correction check on space (Requirement 13)
                if (config.autoCorrection && currentWordToken.length >= 3) {
                    val correction = WordDictionary.getAutoCorrection(currentWordToken)
                    if (correction != null && correction != currentWordToken) {
                        ic.deleteSurroundingText(currentWordToken.length, 0)
                        ic.commitText(correction, 1)
                    }
                }
                ic.commitText(" ", 1)
                currentWordToken = ""
            }
            KeyType.BACKSPACE -> {
                handleBackspace(ic)
                if (currentWordToken.isNotEmpty()) {
                    currentWordToken = currentWordToken.dropLast(1)
                }
            }
            KeyType.ENTER -> {
                handleEnter(ic)
                currentWordToken = ""
            }
            else -> {
                if (key.primaryChar.isNotEmpty()) {
                    ic.commitText(key.primaryChar, 1)
                    currentWordToken = ""
                }
            }
        }
    }

    private fun applySuggestion(word: String) {
        val ic = currentInputConnection ?: return
        if (currentWordToken.isNotEmpty()) {
            ic.deleteSurroundingText(currentWordToken.length, 0)
        }
        ic.commitText("$word ", 1)
        currentWordToken = ""
        val config = preferences.configFlow.value
        if (config.vibrateOnKeypress) {
            hapticManager.vibrateKeypress(config.hapticStrength)
        }
    }

    private fun handleBackspace(ic: android.view.inputmethod.InputConnection) {
        val selectedText = ic.getSelectedText(0)
        if (selectedText.isNullOrEmpty()) {
            val before = ic.getTextBeforeCursor(2, 0)
            if (!before.isNullOrEmpty() && before.length >= 2 && Character.isSurrogatePair(before[before.length - 2], before[before.length - 1])) {
                ic.deleteSurroundingText(2, 0)
            } else {
                ic.deleteSurroundingText(1, 0)
            }
        } else {
            ic.commitText("", 1)
        }
    }

    fun stopBackspaceRepeat() {
        synchronized(backspaceLock) {
            isBackspaceRepeating = false
            backspaceRepeatJob?.cancel()
            backspaceRepeatJob = null
            backspaceRunnable?.let {
                backspaceHandler.removeCallbacks(it)
                backspaceRunnable = null
            }
            backspaceHandler.removeCallbacksAndMessages(null)
            if (::hapticManager.isInitialized) {
                hapticManager.cancel()
            }
        }
    }

    private fun startContinuousBackspace() {
        stopBackspaceRepeat()
        val ic = currentInputConnection ?: return
        val config = preferences.configFlow.value

        synchronized(backspaceLock) {
            isBackspaceRepeating = true
            backspaceRepeatJob = serviceScope.launch {
                try {
                    // Small delay before rapid repeat begins (initial tap already performed 1 deletion and 1 haptic)
                    delay(180)
                    while (isActive && isBackspaceRepeating) {
                        val connection = currentInputConnection ?: break
                        handleBackspace(connection)
                        if (currentWordToken.isNotEmpty()) {
                            currentWordToken = currentWordToken.dropLast(1)
                        }
                        if (config.vibrateOnKeypress && isBackspaceRepeating) {
                            hapticManager.vibrateKeypress(config.hapticStrength)
                        }
                        delay(55)
                    }
                } finally {
                    synchronized(backspaceLock) {
                        isBackspaceRepeating = false
                    }
                }
            }
        }
    }

    private fun handleEnter(ic: android.view.inputmethod.InputConnection) {
        val info = currentInputEditorInfo
        val action = info?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE

        if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(action)
        } else {
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }
    }
}
