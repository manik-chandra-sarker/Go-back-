package com.example.voice

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.util.Log
import com.example.data.model.AppSettings
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Direct On-Device Offline Audio Engine (Dicio-style).
 * Captures raw AudioRecord frames, runs Voice Activity Detection (VAD) with hardware noise
 * suppression, and provides acoustic telemetry.
 */
class OfflineAudioEngine(
    private val onStateChanged: (state: String) -> Unit,
    private val onRmsChanged: (rmsDb: Float) -> Unit,
    private val onSpeechUtteranceCaptured: (audioData: ShortArray, durationMs: Long) -> Unit,
    private val onErrorOccurred: (error: String) -> Unit
) {
    private val isRecording = AtomicBoolean(false)
    private var workerThread: Thread? = null
    private var audioRecord: AudioRecord? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var gainControl: AutomaticGainControl? = null

    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private var currentSettings: AppSettings? = null

    @SuppressLint("MissingPermission")
    fun start(settings: AppSettings) {
        if (isRecording.get()) return
        currentSettings = settings
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val bufferSize = max(minBufferSize * 2, 4096)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                // Fallback to Standard MIC if VOICE_RECOGNITION source unavailable
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )
            }

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                onErrorOccurred("Failed to initialize AudioRecord. Microphone may be busy.")
                return
            }

            // Attach hardware NoiseSuppressor & AGC if supported on device
            val audioSessionId = audioRecord?.audioSessionId ?: 0
            if (audioSessionId != 0 && settings.noiseSuppressionEnabled) {
                try {
                    if (NoiseSuppressor.isAvailable()) {
                        noiseSuppressor = NoiseSuppressor.create(audioSessionId).apply {
                            enabled = true
                        }
                        Log.d(TAG, "Hardware NoiseSuppressor enabled on microphone session")
                    }
                    if (AutomaticGainControl.isAvailable()) {
                        gainControl = AutomaticGainControl.create(audioSessionId).apply {
                            enabled = true
                        }
                        Log.d(TAG, "Hardware AutomaticGainControl enabled on microphone session")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not initialize audio effects: ${e.message}")
                }
            }

            audioRecord?.startRecording()
            isRecording.set(true)
            onStateChanged("LISTENING")

            workerThread = Thread({
                processAudioLoop(bufferSize)
            }, "OfflineAudioEngineThread").apply {
                priority = Thread.NORM_PRIORITY + 2
                start()
            }

            Log.d(TAG, "Offline Audio Engine started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Exception starting OfflineAudioEngine", e)
            onErrorOccurred("Microphone error: ${e.message}")
        }
    }

    fun stop() {
        if (!isRecording.getAndSet(false)) return
        try {
            noiseSuppressor?.release()
            noiseSuppressor = null
            gainControl?.release()
            gainControl = null
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audioRecord", e)
        }
        audioRecord = null
        workerThread?.interrupt()
        workerThread = null
        onStateChanged("STOPPED")
        Log.d(TAG, "Offline Audio Engine stopped")
    }

    private fun processAudioLoop(bufferSize: Int) {
        val audioBuffer = ShortArray(1024)
        val speechAccumulator = ArrayList<Short>()
        var isSpeechActive = false
        var silenceStartTime = 0L
        var ambientNoiseRms = 15.0f
        val silenceTimeout = currentSettings?.silenceTimeoutMs ?: 800L
        val sensitivity = currentSettings?.vadSensitivity ?: 0.5f

        while (isRecording.get()) {
            val record = audioRecord ?: break
            val readCount = record.read(audioBuffer, 0, audioBuffer.size)
            if (readCount <= 0) {
                if (readCount == AudioRecord.ERROR_INVALID_OPERATION || readCount == AudioRecord.ERROR_BAD_VALUE) {
                    onErrorOccurred("Audio read error code $readCount")
                    break
                }
                continue
            }

            var sumSquare = 0.0
            for (i in 0 until readCount) {
                val sample = audioBuffer[i]
                sumSquare += sample * sample
            }
            val rms = sqrt(sumSquare / readCount).toFloat()
            val rmsDb = if (rms > 1f) (20 * log10(rms.toDouble())).toFloat() else 0f
            onRmsChanged(rmsDb)

            val speechThreshold = ambientNoiseRms + (12.0f * (1.1f - sensitivity))

            if (rmsDb > speechThreshold) {
                if (!isSpeechActive) {
                    isSpeechActive = true
                    onStateChanged("SPEECH_DETECTED")
                }
                silenceStartTime = 0L
                if (speechAccumulator.size < sampleRate * 15) {
                    for (i in 0 until readCount) {
                        speechAccumulator.add(audioBuffer[i])
                    }
                }
            } else {
                ambientNoiseRms = ambientNoiseRms * 0.95f + minOf(rmsDb, 35f) * 0.05f
                if (isSpeechActive) {
                    for (i in 0 until readCount) {
                        if (speechAccumulator.size < sampleRate * 15) {
                            speechAccumulator.add(audioBuffer[i])
                        }
                    }
                    if (silenceStartTime == 0L) {
                        silenceStartTime = System.currentTimeMillis()
                    } else if (System.currentTimeMillis() - silenceStartTime >= silenceTimeout) {
                        isSpeechActive = false
                        onStateChanged("PROCESSING")
                        val durationMs = (speechAccumulator.size * 1000L) / sampleRate
                        if (durationMs >= 300L) {
                            val capturedSamples = ShortArray(speechAccumulator.size)
                            for (i in speechAccumulator.indices) {
                                capturedSamples[i] = speechAccumulator[i]
                            }
                            onSpeechUtteranceCaptured(capturedSamples, durationMs)
                        } else {
                            onStateChanged("LISTENING")
                        }
                        speechAccumulator.clear()
                        silenceStartTime = 0L
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "OfflineAudioEngine"
    }
}
