package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class JarvisVoiceEngine(
    private val context: Context,
    private val onSpeechResult: (String) -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel

    init {
        try {
            tts = TextToSpeech(context, this)
        } catch (e: Exception) {
            Log.e("JarvisVoice", "TTS init error: ${e.message}")
        }
    }

    private var onFinishedSpeakingCallback: (() -> Unit)? = null

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            // Try Indian English for ideal Hinglish voice cadence
            val locale = Locale("en", "IN")
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
            tts?.setPitch(1.05f)
            tts?.setSpeechRate(1.04f)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    onFinishedSpeakingCallback?.invoke()
                    onFinishedSpeakingCallback = null
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    onFinishedSpeakingCallback?.invoke()
                    onFinishedSpeakingCallback = null
                }
            })
        }
    }

    fun speak(text: String, onFinished: (() -> Unit)? = null) {
        onFinishedSpeakingCallback = onFinished
        if (!isTtsReady || tts == null) {
            _isSpeaking.value = false
            onFinished?.invoke()
            return
        }

        val cleanText = text
            .replace(Regex("[*#_`>]"), "") // Strip markdown formatting
            .replace("JARVIS", "Jarvis")
        val utteranceId = "jarvis_msg_${System.currentTimeMillis()}"

        _isSpeaking.value = true
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.w("JarvisVoice", "Speech recognition not available on this device")
            return
        }

        try {
            stopSpeaking()
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _isListening.value = true
                        }

                        override fun onBeginningOfSpeech() {
                            _isListening.value = true
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                            // rmsdB typically ranges from -2dB (silence) to 10dB (loud speech)
                            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                            _audioLevel.value = normalized
                        }
                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _isListening.value = false
                            _audioLevel.value = 0f
                        }

                        override fun onError(error: Int) {
                            _isListening.value = false
                            _audioLevel.value = 0f
                            Log.w("JarvisVoice", "Speech recognition error code: $error")
                        }

                        override fun onResults(results: Bundle?) {
                            _isListening.value = false
                            _audioLevel.value = 0f
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val recognized = matches?.firstOrNull() ?: ""
                            if (recognized.isNotBlank()) {
                                onSpeechResult(recognized)
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {}
                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Listening... (Bol boss!)")
            }
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            Log.e("JarvisVoice", "Listening initiation error: ${e.message}")
            _isListening.value = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e("JarvisVoice", "Stop listening error: ${e.message}")
        }
        _isListening.value = false
        _audioLevel.value = 0f
    }

    fun destroy() {
        try {
            tts?.stop()
            tts?.shutdown()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e("JarvisVoice", "Destroy voice engine error: ${e.message}")
        }
    }
}
