package com.example.haptics

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.model.HapticStrength

class KeyWaveHapticManager(context: Context) {
    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (e: Exception) {
        null
    }

    fun vibrateKeypress(strength: HapticStrength) {
        if (strength == HapticStrength.OFF || vibrator == null) return
        try {
            if (!vibrator.hasVibrator()) return

            val duration = when (strength) {
                HapticStrength.LIGHT -> 12L
                HapticStrength.MEDIUM -> 24L
                HapticStrength.STRONG -> 45L
                HapticStrength.OFF -> return
            }

            val amplitude = when (strength) {
                HapticStrength.LIGHT -> 70
                HapticStrength.MEDIUM -> 160
                HapticStrength.STRONG -> 255
                HapticStrength.OFF -> return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (vibrator.hasAmplitudeControl()) {
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        val effectId = when (strength) {
                            HapticStrength.LIGHT -> VibrationEffect.EFFECT_TICK
                            HapticStrength.MEDIUM -> VibrationEffect.EFFECT_CLICK
                            HapticStrength.STRONG -> VibrationEffect.EFFECT_HEAVY_CLICK
                            HapticStrength.OFF -> return
                        }
                        vibrator.vibrate(VibrationEffect.createPredefined(effectId))
                    } catch (_: Throwable) {
                        vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                } else {
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        } catch (_: Throwable) {
            // Silently swallow any device-specific vibration exceptions
        }
    }

    fun cancel() {
        try {
            vibrator?.cancel()
        } catch (_: Throwable) {
        }
    }
}
