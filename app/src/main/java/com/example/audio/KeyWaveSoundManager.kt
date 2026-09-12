package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.example.model.SoundStyle
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class KeyWaveSoundManager(private val context: Context) {
    private var audioManager: AudioManager? = null
    private val staticTracks = ConcurrentHashMap<SoundStyle, Pair<AudioTrack?, AudioTrack?>>()
    private var isDisposed = false
    private var toggle = false

    init {
        try {
            audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            initTracks()
        } catch (_: Throwable) {
            // Graceful fallback to AudioManager clicks
        }
    }

    private fun initTracks() {
        val styles = listOf(
            SoundStyle.MECHANICAL to { generateSamples(frequency = 550.0, durationMs = 28, decay = 85.0, lowPass = true) },
            SoundStyle.SOFT to { generateSamples(frequency = 320.0, durationMs = 22, decay = 110.0, lowPass = true) },
            SoundStyle.CLICK to { generateSamples(frequency = 920.0, durationMs = 18, decay = 120.0, lowPass = false) },
            SoundStyle.TYPEWRITER to { generateSamples(frequency = 1200.0, durationMs = 32, decay = 75.0, lowPass = false, noise = 0.35f) },
            SoundStyle.BUBBLE to { generateSamples(frequency = 700.0, durationMs = 38, decay = 60.0, pitchGlide = true) },
            SoundStyle.MINIMAL to { generateSamples(frequency = 800.0, durationMs = 12, decay = 160.0, lowPass = false) }
        )

        for ((style, generator) in styles) {
            try {
                val samples = generator()
                val track1 = createStaticTrack(samples)
                val track2 = createStaticTrack(samples)
                if (track1 != null || track2 != null) {
                    staticTracks[style] = Pair(track1, track2)
                }
            } catch (_: Throwable) {
                // Ignore failure for specific style, fallback will be used
            }
        }
    }

    private fun createStaticTrack(samples: ShortArray): AudioTrack? {
        return try {
            val sampleRate = 44100
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSizeBytes = if (minBufferSize > 0) {
                maxOf(minBufferSize, samples.size * 2)
            } else {
                samples.size * 2
            }

            val pcmPadded = ShortArray(bufferSizeBytes / 2)
            System.arraycopy(samples, 0, pcmPadded, 0, samples.size)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val track = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(bufferSizeBytes)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            val written = track.write(pcmPadded, 0, pcmPadded.size)
            if (written <= 0) {
                track.release()
                null
            } else {
                track
            }
        } catch (_: Throwable) {
            null
        }
    }

    fun playKeySound(style: SoundStyle, volume: Float) {
        if (isDisposed || style == SoundStyle.OFF || volume <= 0.01f) return
        val effectiveVol = volume.coerceIn(0f, 1f)

        val pair = staticTracks[style]
        if (pair != null) {
            val track = if (toggle) pair.first ?: pair.second else pair.second ?: pair.first
            toggle = !toggle

            if (track != null && track.state == AudioTrack.STATE_INITIALIZED) {
                try {
                    track.pause()
                    track.playbackHeadPosition = 0
                    track.setVolume(effectiveVol)
                    track.play()
                    return
                } catch (_: Throwable) {
                    // Fallback to AudioManager
                }
            }
        }

        // Native Android key click fallback (completely bypasses MediaExtractor/CCodec)
        try {
            audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, effectiveVol)
        } catch (_: Throwable) {
            // Ignored
        }
    }

    fun release() {
        isDisposed = true
        for ((_, pair) in staticTracks) {
            try {
                pair.first?.stop()
                pair.first?.release()
            } catch (_: Throwable) {}
            try {
                pair.second?.stop()
                pair.second?.release()
            } catch (_: Throwable) {}
        }
        staticTracks.clear()
    }

    companion object {
        private fun generateSamples(
            frequency: Double,
            durationMs: Int,
            decay: Double,
            lowPass: Boolean = false,
            pitchGlide: Boolean = false,
            noise: Float = 0f
        ): ShortArray {
            val sampleRate = 44100
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(100)
            val audioData = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val env = exp(-decay * t)
                val currentFreq = if (pitchGlide) frequency * (1.0 + 0.6 * (1.0 - (i.toDouble() / numSamples))) else frequency
                val sine = sin(2.0 * PI * currentFreq * t)
                val noiseVal = if (noise > 0) ((Math.random() * 2.0 - 1.0) * noise) else 0.0
                var sample = (sine + noiseVal) * env

                if (lowPass && i > 0) {
                    sample = (sample * 0.7) + (audioData[i - 1].toDouble() / Short.MAX_VALUE * 0.3)
                }

                audioData[i] = (sample * Short.MAX_VALUE * 0.9).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            return audioData
        }
    }
}
