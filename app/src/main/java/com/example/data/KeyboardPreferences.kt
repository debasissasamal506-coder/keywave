package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CustomGradientConfig
import com.example.model.HapticStrength
import com.example.model.KeyShape
import com.example.model.KeyboardConfig
import com.example.model.KeyboardLanguage
import com.example.model.OneHandedMode
import com.example.model.RGBMode
import com.example.model.SideMargins
import com.example.model.SoundStyle
import com.example.model.ToolbarTool
import com.example.model.WaveColorMode
import com.example.model.WaveIntensity
import com.example.model.WaveSpeed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class KeyboardPreferences(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<KeyboardConfig> = _configFlow.asStateFlow()

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        _configFlow.value = loadConfig()
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    fun loadConfig(): KeyboardConfig {
        val themeId = prefs.getString(KEY_THEME_ID, "minimal_dark") ?: "minimal_dark"
        val rgbMode = try {
            RGBMode.valueOf(prefs.getString(KEY_RGB_MODE, RGBMode.OFF.name) ?: RGBMode.OFF.name)
        } catch (_: Exception) {
            RGBMode.OFF
        }
        val waveSpeed = try {
            WaveSpeed.valueOf(prefs.getString(KEY_WAVE_SPEED, WaveSpeed.MEDIUM.name) ?: WaveSpeed.MEDIUM.name)
        } catch (_: Exception) {
            WaveSpeed.MEDIUM
        }
        val waveIntensity = try {
            WaveIntensity.valueOf(prefs.getString(KEY_WAVE_INTENSITY, WaveIntensity.MEDIUM.name) ?: WaveIntensity.MEDIUM.name)
        } catch (_: Exception) {
            WaveIntensity.MEDIUM
        }
        val waveColorMode = try {
            WaveColorMode.valueOf(prefs.getString(KEY_WAVE_COLOR_MODE, WaveColorMode.SOLID.name) ?: WaveColorMode.SOLID.name)
        } catch (_: Exception) {
            WaveColorMode.SOLID
        }
        val globalWaveColor = prefs.getLong(KEY_GLOBAL_WAVE_COLOR, 0xFF00F5D4)
        val waveOpacity = prefs.getFloat(KEY_WAVE_OPACITY, 0.85f)
        val keyShape = try {
            KeyShape.valueOf(prefs.getString(KEY_SHAPE, KeyShape.SOFT_ROUNDED.name) ?: KeyShape.SOFT_ROUNDED.name)
        } catch (_: Exception) {
            KeyShape.SOFT_ROUNDED
        }
        val sideMargins = try {
            SideMargins.valueOf(prefs.getString(KEY_SIDE_MARGINS, SideMargins.NORMAL.name) ?: SideMargins.NORMAL.name)
        } catch (_: Exception) {
            SideMargins.NORMAL
        }
        val hapticStrength = try {
            HapticStrength.valueOf(prefs.getString(KEY_HAPTIC, HapticStrength.LIGHT.name) ?: HapticStrength.LIGHT.name)
        } catch (_: Exception) {
            HapticStrength.LIGHT
        }
        val soundStyle = try {
            SoundStyle.valueOf(prefs.getString(KEY_SOUND_STYLE, SoundStyle.SOFT.name) ?: SoundStyle.SOFT.name)
        } catch (_: Exception) {
            SoundStyle.SOFT
        }

        val oneHandedMode = try {
            OneHandedMode.valueOf(prefs.getString(KEY_ONE_HANDED, OneHandedMode.OFF.name) ?: OneHandedMode.OFF.name)
        } catch (_: Exception) {
            OneHandedMode.OFF
        }

        val currentLanguage = try {
            KeyboardLanguage.valueOf(prefs.getString(KEY_LANGUAGE, KeyboardLanguage.ENGLISH.name) ?: KeyboardLanguage.ENGLISH.name)
        } catch (_: Exception) {
            KeyboardLanguage.ENGLISH
        }

        val visibleToolsRaw = prefs.getString(KEY_VISIBLE_TOOLS, null)
        val visibleToolbarTools = if (!visibleToolsRaw.isNullOrBlank()) {
            visibleToolsRaw.split(",").mapNotNull { name ->
                try { ToolbarTool.valueOf(name.trim()) } catch (_: Exception) { null }
            }.ifEmpty {
                listOf(
                    ToolbarTool.EMOJI,
                    ToolbarTool.CLIPBOARD,
                    ToolbarTool.TEXT_EDITING,
                    ToolbarTool.ONE_HANDED,
                    ToolbarTool.VOICE,
                    ToolbarTool.SETTINGS
                )
            }
        } else {
            listOf(
                ToolbarTool.EMOJI,
                ToolbarTool.CLIPBOARD,
                ToolbarTool.TEXT_EDITING,
                ToolbarTool.ONE_HANDED,
                ToolbarTool.VOICE,
                ToolbarTool.SETTINGS
            )
        }

        return KeyboardConfig(
            themeId = themeId,
            rgbMode = rgbMode,
            waveSpeed = waveSpeed,
            waveIntensity = waveIntensity,
            waveColorMode = waveColorMode,
            globalWaveColor = globalWaveColor,
            waveOpacity = waveOpacity,
            rgbBrightness = prefs.getFloat(KEY_BRIGHTNESS, 0.60f),
            animationSpeedMultiplier = prefs.getFloat(KEY_ANIM_SPEED, 1.0f),
            keyboardSizePercent = prefs.getInt(KEY_SIZE_PERCENT, 100),
            keyHeightDp = prefs.getFloat(KEY_HEIGHT_DP, 48f),
            sideMargins = sideMargins,
            keyShape = keyShape,
            minimalisticMode = prefs.getBoolean(KEY_MINIMALISTIC, false),
            glassMode = prefs.getBoolean(KEY_GLASS, false),
            performanceMode = prefs.getBoolean(KEY_PERFORMANCE, false),
            batterySaverMode = prefs.getBoolean(KEY_BATTERY_SAVER, false),
            hapticStrength = hapticStrength,
            soundStyle = soundStyle,
            soundVolume = prefs.getFloat(KEY_SOUND_VOLUME, 0.5f),
            useCustomColors = prefs.getBoolean(KEY_USE_CUSTOM_COLORS, false),
            customBackground = prefs.getLong(KEY_CUSTOM_BG, 0xFF12131A),
            customKeyColor = prefs.getLong(KEY_CUSTOM_KEY_COLOR, 0xFF1E202B),
            customTextColor = prefs.getLong(KEY_CUSTOM_TEXT_COLOR, 0xFFF1F5F9),
            customAccent = prefs.getLong(KEY_CUSTOM_ACCENT, 0xFF38BDF8),
            customGlowColor = prefs.getLong(KEY_CUSTOM_GLOW_COLOR, 0xFF38BDF8),
            customSecondaryText = prefs.getLong(KEY_CUSTOM_SEC_TEXT, 0xFF94A3B8),
            customSpecialKeyColor = prefs.getLong(KEY_CUSTOM_SPECIAL_KEY, 0xFF181924),
            customPrimary = prefs.getLong(KEY_CUSTOM_PRIMARY, 0xFF38BDF8),
            customSecondary = prefs.getLong(KEY_CUSTOM_SECONDARY, 0xFF818CF8),
            customGradient = CustomGradientConfig(
                type = prefs.getString(KEY_GRADIENT_TYPE, "2-color") ?: "2-color",
                color1 = prefs.getLong(KEY_GRADIENT_C1, 0xFF38BDF8),
                color2 = prefs.getLong(KEY_GRADIENT_C2, 0xFF818CF8),
                color3 = prefs.getLong(KEY_GRADIENT_C3, 0xFFC084FC),
                directionAngle = prefs.getFloat(KEY_GRADIENT_ANGLE, 45f),
                speed = prefs.getFloat(KEY_GRADIENT_SPEED, 1.0f),
                brightness = prefs.getFloat(KEY_GRADIENT_BRIGHTNESS, 0.8f),
                intensity = prefs.getFloat(KEY_GRADIENT_INTENSITY, 0.9f)
            ),
            useGradientColors = prefs.getBoolean(KEY_USE_GRADIENT, false),
            showNumberRow = prefs.getBoolean(KEY_SHOW_NUMBER_ROW, true),
            showSuggestions = prefs.getBoolean(KEY_SHOW_SUGGESTIONS, true),
            autoCorrection = prefs.getBoolean(KEY_AUTO_CORRECTION, false),
            autoCapitalize = prefs.getBoolean(KEY_AUTO_CAPITALIZE, true),
            soundOnKeypress = prefs.getBoolean(KEY_SOUND_ON_KEYPRESS, false),
            vibrateOnKeypress = prefs.getBoolean(KEY_VIBRATE_ON_KEYPRESS, true),
            showToolbar = prefs.getBoolean(KEY_SHOW_TOOLBAR, true),
            oneHandedMode = oneHandedMode,
            currentLanguage = currentLanguage,
            visibleToolbarTools = visibleToolbarTools,
            saveClipboardHistory = prefs.getBoolean(KEY_SAVE_CLIPBOARD_HISTORY, true),
            keyboardWidthPercent = prefs.getInt(KEY_KEYBOARD_WIDTH_PERCENT, 100),
            keySpacingDp = prefs.getFloat(KEY_KEY_SPACING_DP, 4f)
        )
    }

    fun updateConfig(update: (KeyboardConfig) -> KeyboardConfig) {
        val current = _configFlow.value
        val newConfig = update(current)
        saveConfig(newConfig)
    }

    fun saveConfig(config: KeyboardConfig) {
        prefs.edit()
            .putString(KEY_THEME_ID, config.themeId)
            .putString(KEY_RGB_MODE, config.rgbMode.name)
            .putString(KEY_WAVE_SPEED, config.waveSpeed.name)
            .putString(KEY_WAVE_INTENSITY, config.waveIntensity.name)
            .putString(KEY_WAVE_COLOR_MODE, config.waveColorMode.name)
            .putLong(KEY_GLOBAL_WAVE_COLOR, config.globalWaveColor)
            .putFloat(KEY_WAVE_OPACITY, config.waveOpacity)
            .putFloat(KEY_BRIGHTNESS, config.rgbBrightness)
            .putFloat(KEY_ANIM_SPEED, config.animationSpeedMultiplier)
            .putInt(KEY_SIZE_PERCENT, config.keyboardSizePercent)
            .putFloat(KEY_HEIGHT_DP, config.keyHeightDp)
            .putString(KEY_SIDE_MARGINS, config.sideMargins.name)
            .putString(KEY_SHAPE, config.keyShape.name)
            .putBoolean(KEY_MINIMALISTIC, config.minimalisticMode)
            .putBoolean(KEY_GLASS, config.glassMode)
            .putBoolean(KEY_PERFORMANCE, config.performanceMode)
            .putBoolean(KEY_BATTERY_SAVER, config.batterySaverMode)
            .putString(KEY_HAPTIC, config.hapticStrength.name)
            .putString(KEY_SOUND_STYLE, config.soundStyle.name)
            .putFloat(KEY_SOUND_VOLUME, config.soundVolume)
            .putBoolean(KEY_USE_CUSTOM_COLORS, config.useCustomColors)
            .putLong(KEY_CUSTOM_BG, config.customBackground)
            .putLong(KEY_CUSTOM_KEY_COLOR, config.customKeyColor)
            .putLong(KEY_CUSTOM_TEXT_COLOR, config.customTextColor)
            .putLong(KEY_CUSTOM_ACCENT, config.customAccent)
            .putLong(KEY_CUSTOM_GLOW_COLOR, config.customGlowColor)
            .putLong(KEY_CUSTOM_SEC_TEXT, config.customSecondaryText)
            .putLong(KEY_CUSTOM_SPECIAL_KEY, config.customSpecialKeyColor)
            .putLong(KEY_CUSTOM_PRIMARY, config.customPrimary)
            .putLong(KEY_CUSTOM_SECONDARY, config.customSecondary)
            .putString(KEY_GRADIENT_TYPE, config.customGradient.type)
            .putLong(KEY_GRADIENT_C1, config.customGradient.color1)
            .putLong(KEY_GRADIENT_C2, config.customGradient.color2)
            .putLong(KEY_GRADIENT_C3, config.customGradient.color3)
            .putFloat(KEY_GRADIENT_ANGLE, config.customGradient.directionAngle)
            .putFloat(KEY_GRADIENT_SPEED, config.customGradient.speed)
            .putFloat(KEY_GRADIENT_BRIGHTNESS, config.customGradient.brightness)
            .putFloat(KEY_GRADIENT_INTENSITY, config.customGradient.intensity)
            .putBoolean(KEY_USE_GRADIENT, config.useGradientColors)
            .putBoolean(KEY_SHOW_NUMBER_ROW, config.showNumberRow)
            .putBoolean(KEY_SHOW_SUGGESTIONS, config.showSuggestions)
            .putBoolean(KEY_AUTO_CORRECTION, config.autoCorrection)
            .putBoolean(KEY_AUTO_CAPITALIZE, config.autoCapitalize)
            .putBoolean(KEY_SOUND_ON_KEYPRESS, config.soundOnKeypress)
            .putBoolean(KEY_VIBRATE_ON_KEYPRESS, config.vibrateOnKeypress)
            .putBoolean(KEY_SHOW_TOOLBAR, config.showToolbar)
            .putString(KEY_ONE_HANDED, config.oneHandedMode.name)
            .putString(KEY_LANGUAGE, config.currentLanguage.name)
            .putString(KEY_VISIBLE_TOOLS, config.visibleToolbarTools.joinToString(",") { it.name })
            .putBoolean(KEY_SAVE_CLIPBOARD_HISTORY, config.saveClipboardHistory)
            .putInt(KEY_KEYBOARD_WIDTH_PERCENT, config.keyboardWidthPercent)
            .putFloat(KEY_KEY_SPACING_DP, config.keySpacingDp)
            .apply()

        _configFlow.value = config
    }

    companion object {
        private const val PREFS_NAME = "keywave_preferences"
        private const val KEY_THEME_ID = "theme_id"
        private const val KEY_RGB_MODE = "rgb_mode"
        private const val KEY_WAVE_SPEED = "wave_speed"
        private const val KEY_WAVE_INTENSITY = "wave_intensity"
        private const val KEY_WAVE_COLOR_MODE = "wave_color_mode"
        private const val KEY_GLOBAL_WAVE_COLOR = "global_wave_color"
        private const val KEY_WAVE_OPACITY = "wave_opacity"
        private const val KEY_BRIGHTNESS = "rgb_brightness"
        private const val KEY_ANIM_SPEED = "anim_speed"
        private const val KEY_SIZE_PERCENT = "size_percent"
        private const val KEY_HEIGHT_DP = "height_dp"
        private const val KEY_SIDE_MARGINS = "side_margins"
        private const val KEY_SHAPE = "key_shape"
        private const val KEY_MINIMALISTIC = "minimalistic_mode"
        private const val KEY_GLASS = "glass_mode"
        private const val KEY_PERFORMANCE = "performance_mode"
        private const val KEY_BATTERY_SAVER = "battery_saver_mode"
        private const val KEY_HAPTIC = "haptic_strength"
        private const val KEY_SOUND_STYLE = "sound_style"
        private const val KEY_SOUND_VOLUME = "sound_volume"
        private const val KEY_USE_CUSTOM_COLORS = "use_custom_colors"
        private const val KEY_CUSTOM_BG = "custom_bg"
        private const val KEY_CUSTOM_KEY_COLOR = "custom_key_color"
        private const val KEY_CUSTOM_TEXT_COLOR = "custom_text_color"
        private const val KEY_CUSTOM_ACCENT = "custom_accent"
        private const val KEY_CUSTOM_GLOW_COLOR = "custom_glow_color"
        private const val KEY_CUSTOM_SEC_TEXT = "custom_sec_text"
        private const val KEY_CUSTOM_SPECIAL_KEY = "custom_special_key"
        private const val KEY_CUSTOM_PRIMARY = "custom_primary"
        private const val KEY_CUSTOM_SECONDARY = "custom_secondary"
        private const val KEY_GRADIENT_TYPE = "gradient_type"
        private const val KEY_GRADIENT_C1 = "gradient_c1"
        private const val KEY_GRADIENT_C2 = "gradient_c2"
        private const val KEY_GRADIENT_C3 = "gradient_c3"
        private const val KEY_GRADIENT_ANGLE = "gradient_angle"
        private const val KEY_GRADIENT_SPEED = "gradient_speed"
        private const val KEY_GRADIENT_BRIGHTNESS = "gradient_brightness"
        private const val KEY_GRADIENT_INTENSITY = "gradient_intensity"
        private const val KEY_USE_GRADIENT = "use_gradient"
        private const val KEY_SHOW_NUMBER_ROW = "show_number_row"
        private const val KEY_SHOW_SUGGESTIONS = "show_suggestions"
        private const val KEY_AUTO_CORRECTION = "auto_correction"
        private const val KEY_AUTO_CAPITALIZE = "auto_capitalize"
        private const val KEY_SOUND_ON_KEYPRESS = "sound_on_keypress"
        private const val KEY_VIBRATE_ON_KEYPRESS = "vibrate_on_keypress"
        private const val KEY_SHOW_TOOLBAR = "show_toolbar"
        private const val KEY_ONE_HANDED = "one_handed_mode"
        private const val KEY_LANGUAGE = "current_language"
        private const val KEY_VISIBLE_TOOLS = "visible_toolbar_tools"
        private const val KEY_SAVE_CLIPBOARD_HISTORY = "save_clipboard_history"
        private const val KEY_KEYBOARD_WIDTH_PERCENT = "keyboard_width_percent"
        private const val KEY_KEY_SPACING_DP = "key_spacing_dp"

        @Volatile
        private var instance: KeyboardPreferences? = null

        fun getInstance(context: Context): KeyboardPreferences {
            return instance ?: synchronized(this) {
                instance ?: KeyboardPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
