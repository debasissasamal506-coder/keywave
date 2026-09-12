package com.example.model

enum class WaveColorMode(val displayName: String) {
    SOLID("Solid Color"),
    RAINBOW("RGB / Rainbow")
}

enum class GradientType(val displayName: String) {
    SINGLE("Single Color"),
    TWO_COLOR("2 Color Gradient"),
    THREE_COLOR("3 Color Gradient"),
    RAINBOW("Rainbow")
}

data class CustomGradientConfig(
    val type: String = "2-color", // "single", "2-color", "3-color", "rainbow"
    val color1: Long = 0xFF00F5D4,
    val color2: Long = 0xFF7B2CBF,
    val color3: Long = 0xFFFF007F,
    val directionAngle: Float = 45f,
    val speed: Float = 1.0f,
    val brightness: Float = 0.8f,
    val intensity: Float = 0.9f
)

data class KeyboardTheme(
    val id: String,
    val name: String,
    val backgroundColor: Long,
    val keyColor: Long,
    val keyActiveColor: Long,
    val specialKeyColor: Long,
    val textColor: Long,
    val specialTextColor: Long,
    val accentColor: Long,
    val secondaryColor: Long,
    val glowColor: Long,
    val borderColor: Long,
    val defaultRgbMode: RGBMode = RGBMode.REACTIVE
)

enum class RGBMode(val displayName: String, val description: String) {
    OFF("OFF", "Clean stealth mode with no lighting"),
    STATIC("Static", "Clean elegant solid accent lighting"),
    REACTIVE("Reactive", "Light ripples out from the pressed key"),
    RGB_WAVE("Wave", "Concentric wave traveling across keys"),
    RAINBOW("Rainbow", "Smooth continuous chromatic spectrum flow"),
    AURORA("Aurora", "Ethereal northern lights shifting glow"),
    BREATHING("Breathing", "Rhythmic gentle color pulsation"),
    PULSE("Pulse", "Dynamic energy wave bursting outward"),
    NEON("Neon", "Electric vibrant cyber glow"),
    FIRE("Fire", "Warm glowing embers and flame accents"),
    OCEAN("Ocean", "Deep marine blues and aqua swells"),
    MINIMAL_GLOW("Minimal Glow", "Subtle soft ambient edge light")
}

enum class KeyShape(val displayName: String, val cornerRadiusDp: Float) {
    ROUNDED("Rounded", 10f),
    SOFT_ROUNDED("Soft Rounded", 6f),
    PILL("Pill", 999f),
    MINIMAL("Minimal", 2f)
}

enum class HapticStrength(val displayName: String, val durationMs: Long, val amplitude: Int) {
    OFF("OFF", 0L, 0),
    LIGHT("Light", 10L, 50),
    MEDIUM("Medium", 20L, 120),
    STRONG("Strong", 38L, 220)
}

enum class SoundStyle(val displayName: String) {
    OFF("OFF"),
    SOFT("Soft"),
    CLICK("Click"),
    MECHANICAL("Mechanical"),
    TYPEWRITER("Typewriter"),
    BUBBLE("Bubble"),
    MINIMAL("Minimal")
}

enum class WaveSpeed(val displayName: String, val speedFactor: Float) {
    SLOW("Slow", 0.6f),
    MEDIUM("Medium", 1.0f),
    FAST("Fast", 1.5f)
}

enum class WaveIntensity(val displayName: String, val multiplier: Float) {
    LOW("Low", 0.5f),
    MEDIUM("Medium", 0.8f),
    HIGH("High", 1.0f)
}

enum class SideMargins(val displayName: String, val paddingDp: Float) {
    COMPACT("Compact", 12f),
    NORMAL("Normal", 4f),
    WIDE("Wide", 0f)
}

enum class KeyboardSizeOption(val displayName: String, val percent: Int) {
    SMALL("Small", 85),
    MEDIUM("Medium", 100),
    LARGE("Large", 115)
}

enum class KeyHeightOption(val displayName: String, val heightDp: Float) {
    COMPACT("Compact", 42f),
    NORMAL("Normal", 48f),
    TALL("Tall", 56f)
}

data class KeyboardConfig(
    // Default appearance is clean, minimal, dark, elegant & subtle
    val themeId: String = "minimal_dark",
    val rgbMode: RGBMode = RGBMode.OFF,
    val waveSpeed: WaveSpeed = WaveSpeed.MEDIUM,
    val waveIntensity: WaveIntensity = WaveIntensity.MEDIUM,
    // Global Wave Color System
    val waveColorMode: WaveColorMode = WaveColorMode.SOLID,
    val globalWaveColor: Long = 0xFF00F5D4,
    val waveOpacity: Float = 0.85f,
    val rgbBrightness: Float = 0.60f, // 0.0 to 1.0
    val animationSpeedMultiplier: Float = 1.0f,
    val keyboardSizePercent: Int = 100, // 70 to 120
    val keyHeightDp: Float = 48f, // 40 to 60
    val sideMargins: SideMargins = SideMargins.NORMAL,
    val keyShape: KeyShape = KeyShape.SOFT_ROUNDED,
    
    // Feature Modes
    val minimalisticMode: Boolean = false,
    val glassMode: Boolean = false,
    val performanceMode: Boolean = false,
    val batterySaverMode: Boolean = false,
    
    // Haptic & Sound
    val hapticStrength: HapticStrength = HapticStrength.LIGHT,
    val soundStyle: SoundStyle = SoundStyle.SOFT,
    val soundVolume: Float = 0.5f, // 0.0 to 1.0
    
    // Custom Colors (overrides theme when useCustomColors is true)
    val useCustomColors: Boolean = false,
    val customBackground: Long = 0xFF12131A,
    val customKeyColor: Long = 0xFF1E202B,
    val customTextColor: Long = 0xFFF1F5F9,
    val customAccent: Long = 0xFF38BDF8,
    val customGlowColor: Long = 0xFF38BDF8,
    val customSecondaryText: Long = 0xFF94A3B8,
    val customSpecialKeyColor: Long = 0xFF181924,
    val customPrimary: Long = 0xFF38BDF8, // backwards compatibility alias
    val customSecondary: Long = 0xFF818CF8,
    
    val customGradient: CustomGradientConfig = CustomGradientConfig(),
    val useGradientColors: Boolean = false,
    
    // Keyboard functional options
    val showNumberRow: Boolean = true,
    val showSuggestions: Boolean = true,
    val autoCorrection: Boolean = false,
    val autoCapitalize: Boolean = true,
    val soundOnKeypress: Boolean = false,
    val vibrateOnKeypress: Boolean = true,
    
    // Toolbar, Language & Layout Enhancements (Requirement 32)
    val showToolbar: Boolean = true,
    val oneHandedMode: OneHandedMode = OneHandedMode.OFF,
    val currentLanguage: KeyboardLanguage = KeyboardLanguage.ENGLISH,
    val visibleToolbarTools: List<ToolbarTool> = listOf(
        ToolbarTool.EMOJI,
        ToolbarTool.CLIPBOARD,
        ToolbarTool.TEXT_EDITING,
        ToolbarTool.ONE_HANDED,
        ToolbarTool.VOICE,
        ToolbarTool.SETTINGS
    ),
    val saveClipboardHistory: Boolean = true,
    val keyboardWidthPercent: Int = 100, // 75 to 100
    val keySpacingDp: Float = 4f // 2 to 6
) {
    val scaleFactor: Float
        get() = (keyboardSizePercent.coerceIn(70, 120) / 100f)

    val effectiveBrightness: Float
        get() = if (batterySaverMode) (rgbBrightness * 0.4f).coerceAtLeast(0.1f) else rgbBrightness

    val effectiveGlowIntensity: Float
        get() = when {
            minimalisticMode -> 0.12f
            batterySaverMode -> 0.25f
            performanceMode -> 0.35f
            else -> waveIntensity.multiplier
        }
}
