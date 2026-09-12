package com.example.keyboard.tools

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.util.Locale

class VoiceInputHelper(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onStatus: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    var isListening = false
        private set

    fun startListening() {
        // 1. Check microphone permission
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            onStatus("Microphone permission required for voice input")
            Toast.makeText(context, "Please grant microphone permission to use voice input", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Check if SpeechRecognizer service is available on device
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onStatus("Speech recognition service not available on this device")
            Toast.makeText(context, "Speech recognition is not supported on this device", Toast.LENGTH_SHORT).show()
            return
        }

        stopListening()

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListening = true
                        onStatus("Listening...")
                    }

                    override fun onBeginningOfSpeech() {
                        onStatus("Hearing voice...")
                    }

                    override fun onRmsChanged(rmsdB: Float) {}

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        isListening = false
                        onStatus("Processing...")
                    }

                    override fun onError(error: Int) {
                        isListening = false
                        val message = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_CLIENT -> "Voice client error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                            SpeechRecognizer.ERROR_NETWORK -> "Network error occurred"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service busy"
                            SpeechRecognizer.ERROR_SERVER -> "Recognition server error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                            else -> "Voice input error ($error)"
                        }
                        onStatus(message)
                    }

                    override fun onResults(results: Bundle?) {
                        isListening = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim()
                        if (!text.isNullOrEmpty()) {
                            onResult(text)
                            onStatus("Voice input added")
                        } else {
                            onStatus("No speech detected")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                        if (!partial.isNullOrEmpty()) {
                            onStatus(partial)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }

            speechRecognizer?.startListening(intent)
            isListening = true
            onStatus("Listening...")
        } catch (e: Exception) {
            isListening = false
            onStatus("Could not start voice input")
        }
    }

    fun stopListening() {
        try {
            if (isListening) {
                speechRecognizer?.stopListening()
            }
            speechRecognizer?.destroy()
            speechRecognizer = null
            isListening = false
        } catch (_: Exception) {}
    }
}
