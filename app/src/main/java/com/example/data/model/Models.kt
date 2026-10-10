package com.example.data.model

enum class MatchMode(val label: String) {
    CONTAINS("Contains phrase"),
    EXACT("Exact match"),
    STARTS_WITH("Starts with"),
    ANY("Catch-all (Any speech)")
}

enum class TargetApp(val displayName: String, val defaultAction: String, val defaultExtra: String) {
    TASKER(
        displayName = "Tasker",
        defaultAction = "net.dinglisch.android.tasker.ACTION_SPEECH_COMMAND",
        defaultExtra = "voice_command"
    ),
    MACRODROID(
        displayName = "MacroDroid",
        defaultAction = "com.arlosoft.macrodroid.intent.action.TRIGGER",
        defaultExtra = "command"
    ),
    CUSTOM(
        displayName = "Custom Broadcast",
        defaultAction = "com.voicecontrol.ACTION_COMMAND",
        defaultExtra = "text"
    )
}

enum class VoskModelVariant(
    val id: String,
    val displayName: String,
    val sizeDescription: String,
    val accuracyRating: String,
    val downloadUrl: String,
    val dirName: String
) {
    VOSK_SMALL_EN(
        id = "small_en",
        displayName = "Vosk Small (Compact)",
        sizeDescription = "~40 MB download",
        accuracyRating = "Good for simple words, fast & low RAM",
        downloadUrl = "https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip",
        dirName = "vosk-model-small-en-us"
    ),
    VOSK_HIGH_ACCURACY_LGRAPH(
        id = "lgraph_en",
        displayName = "Vosk High-Accuracy (Kaldi LGraph)",
        sizeDescription = "~128 MB download",
        accuracyRating = "Significantly higher accuracy with dynamic Lookahead Graph",
        downloadUrl = "https://alphacephei.com/vosk/models/vosk-model-en-us-0.22-lgraph.zip",
        dirName = "vosk-model-en-us-0.22-lgraph"
    )
}

enum class OfflineEngineMode(val title: String, val subtitle: String) {
    VOSK_OFFLINE(
        title = "Vosk Offline STT (True Speech-to-Text)",
        subtitle = "Transforms spoken words into text 100% offline using embedded Vosk engine"
    ),
    STANDALONE_AUDIO(
        title = "Direct Acoustic VAD Engine (Dicio-style)",
        subtitle = "100% on-device AudioRecord VAD & acoustic phrase matcher"
    ),
    HYBRID_AUTO(
        title = "Hybrid Auto (Vosk + Fallbacks)",
        subtitle = "Uses Vosk offline STT, automatically falls back to Direct Audio Engine if needed"
    ),
    GOOGLE_STT(
        title = "Google SpeechRecognizer (Offline Mode)",
        subtitle = "Relies on downloaded Google voice acoustic language packs"
    )
}

data class AppSettings(
    val preferOffline: Boolean = true,
    val engineMode: OfflineEngineMode = OfflineEngineMode.VOSK_OFFLINE,
    val selectedVoskModel: VoskModelVariant = VoskModelVariant.VOSK_HIGH_ACCURACY_LGRAPH,
    val useGrammarConstraint: Boolean = true,
    val noiseSuppressionEnabled: Boolean = true,
    val vadSensitivity: Float = 0.5f,
    val silenceTimeoutMs: Long = 800L,
    val commandDelayMs: Long = 1500L,
    val stitchMultiSegmentSpeech: Boolean = true,
    val speechLanguage: String = "en-US",
    val wakeWordEnabled: Boolean = false,
    val wakeWord: String = "computer",
    val defaultTarget: TargetApp = TargetApp.TASKER,
    val defaultAction: String = "net.dinglisch.android.tasker.ACTION_SPEECH_COMMAND",
    val defaultExtraKey: String = "voice_command",
    val defaultPackage: String = "",
    val ttsFeedbackEnabled: Boolean = true,
    val ttsPitch: Float = 1.0f,
    val ttsSpeechRate: Float = 1.0f,
    val ttsTemplate: String = "Command received: %s",
    val startOnBoot: Boolean = false
)

enum class ListeningStatus(val label: String) {
    STOPPED("Stopped"),
    INITIALIZING("Starting..."),
    LISTENING("Listening (Microphone Active)"),
    SPEECH_DETECTED("Hearing Speech..."),
    PROCESSING("Transcribing..."),
    DISPATCHING("Sending Intent..."),
    SPEAKING("Speaking Feedback..."),
    ERROR("Error / Retrying")
}

data class ServiceState(
    val isRunning: Boolean = false,
    val status: ListeningStatus = ListeningStatus.STOPPED,
    val activeEngine: String = "Offline Audio",
    val activeModelName: String = "",
    val grammarModeActive: Boolean = false,
    val isBufferingCommand: Boolean = false,
    val bufferRemainingMs: Long = 0L,
    val rmsDb: Float = 0f,
    val partialTranscript: String = "",
    val lastRecognizedText: String = "",
    val lastRecognizedConfidence: Float = 0f,
    val lastRecognizedTime: Long = 0L,
    val lastDispatchedTarget: String = "",
    val lastDispatchedAction: String = "",
    val lastDispatchedExtras: String = "",
    val errorMessage: String? = null
)
