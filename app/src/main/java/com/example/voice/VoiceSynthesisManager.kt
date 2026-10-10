package com.example.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import com.example.data.model.AppSettings
import java.util.Locale

class VoiceSynthesisManager(
    private val context: Context,
    private val onSpeechStatusChanged: (isSpeaking: Boolean) -> Unit = {}
) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "TTS: Language not supported or missing data, trying default")
                tts?.setLanguage(Locale.getDefault())
            }
            isInitialized = true
            selectMaleVoice()
            setupProgressListener()
            Log.d(TAG, "TTS initialized successfully with natural male voice")
        } else {
            Log.e(TAG, "TTS initialization failed: $status")
        }
    }

    /**
     * Finds and selects a natural Male English voice.
     * Google TTS and standard Android engines tag voices with "male", "en-us-x-sfg#male", etc.
     */
    private fun selectMaleVoice() {
        try {
            val voices = tts?.voices ?: return
            val englishVoices = voices.filter { voice ->
                voice.locale.language.equals("en", ignoreCase = true)
            }

            // Find explicit male voice by name or feature
            val maleVoice = englishVoices.firstOrNull { voice ->
                val nameLower = voice.name.lowercase()
                (nameLower.contains("male") ||
                 nameLower.contains("#male") ||
                 nameLower.contains("-male") ||
                 nameLower.contains("en-us-x-sfg") ||
                 nameLower.contains("en-us-x-iol") ||
                 nameLower.contains("en-us-x-tpd") ||
                 nameLower.contains("en-gb-x-rjs")) &&
                !nameLower.contains("female")
            } ?: englishVoices.firstOrNull { voice ->
                // Check if voice features indicate male
                voice.features?.any { it.contains("male", ignoreCase = true) } == true
            }

            if (maleVoice != null) {
                tts?.voice = maleVoice
                Log.d(TAG, "Selected Male voice: ${maleVoice.name}")
            } else {
                Log.d(TAG, "No explicit male voice metadata found, using default US English with natural tone")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not set male voice: ${e.message}")
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                onSpeechStatusChanged(true)
            }

            override fun onDone(utteranceId: String?) {
                onSpeechStatusChanged(false)
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                onSpeechStatusChanged(false)
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                onSpeechStatusChanged(false)
            }
        })
    }

    fun speak(text: String, settings: AppSettings) {
        if (!settings.ttsFeedbackEnabled || text.isBlank()) return
        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized yet")
            return
        }
        try {
            // Keep natural voice tone: pitch = 1.0f (no artificial modification)
            tts?.setPitch(1.0f)
            tts?.setSpeechRate(settings.ttsSpeechRate.coerceIn(0.8f, 1.2f))
            selectMaleVoice()

            val utteranceId = "voice_bridge_${System.currentTimeMillis()}"
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e(TAG, "Error speaking text", e)
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }
        onSpeechStatusChanged(false)
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS", e)
        }
        tts = null
        isInitialized = false
    }

    companion object {
        private const val TAG = "VoiceSynthesisManager"
    }
}
