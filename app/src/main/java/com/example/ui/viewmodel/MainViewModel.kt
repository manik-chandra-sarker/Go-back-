package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SettingsPreferences
import com.example.data.db.AppDatabase
import com.example.data.db.CommandLogEntity
import com.example.data.db.VoiceRuleEntity
import com.example.data.model.AppSettings
import com.example.data.model.ServiceState
import com.example.data.model.VoskModelVariant
import com.example.service.VoiceCaptureService
import com.example.voice.VoiceSynthesisManager
import com.example.voice.VoskManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val prefs = SettingsPreferences.getInstance(application)
    private val ttsTester = VoiceSynthesisManager(application)

    private val _isLGraphInstalled = MutableStateFlow(checkInstalled(VoskModelVariant.VOSK_HIGH_ACCURACY_LGRAPH))
    val isLGraphInstalled: StateFlow<Boolean> = _isLGraphInstalled.asStateFlow()

    private val _isSmallInstalled = MutableStateFlow(checkInstalled(VoskModelVariant.VOSK_SMALL_EN))
    val isSmallInstalled: StateFlow<Boolean> = _isSmallInstalled.asStateFlow()

    private val _fallbackVoskState = MutableStateFlow<VoskManager.ModelState>(
        if (_isLGraphInstalled.value)
            VoskManager.ModelState.Ready(VoskModelVariant.VOSK_HIGH_ACCURACY_LGRAPH)
        else if (_isSmallInstalled.value)
            VoskManager.ModelState.Ready(VoskModelVariant.VOSK_SMALL_EN)
        else
            VoskManager.ModelState.NotInstalled
    )

    val serviceState: StateFlow<ServiceState> = VoiceCaptureService.serviceState
    val settings: StateFlow<AppSettings> = prefs.settingsFlow

    val rules: StateFlow<List<VoiceRuleEntity>> = db.voiceRuleDao()
        .getAllRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logs: StateFlow<List<CommandLogEntity>> = db.commandLogDao()
        .getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val voskModelState: StateFlow<VoskManager.ModelState>
        get() = VoiceCaptureService.voskManagerInstance?.modelState ?: _fallbackVoskState.asStateFlow()

    private fun checkInstalled(variant: VoskModelVariant): Boolean {
        val dir = File(getApplication<Application>().filesDir, variant.dirName)
        if (!dir.exists()) return false
        val files = dir.list() ?: return false
        return files.any { it.contains("am") || it.contains("conf") || it.contains("model") || it.contains("final.mdl") }
    }

    fun refreshInstalledStatus() {
        _isLGraphInstalled.value = checkInstalled(VoskModelVariant.VOSK_HIGH_ACCURACY_LGRAPH)
        _isSmallInstalled.value = checkInstalled(VoskModelVariant.VOSK_SMALL_EN)
    }

    fun downloadModel(variant: VoskModelVariant) {
        val manager = VoiceCaptureService.voskManagerInstance
        if (manager != null) {
            manager.downloadModel(variant, viewModelScope)
            viewModelScope.launch {
                manager.modelState.collect { state ->
                    if (state is VoskManager.ModelState.Ready) {
                        refreshInstalledStatus()
                        updateSettings(settings.value.copy(selectedVoskModel = variant))
                    }
                }
            }
        } else {
            val standalone = VoskManager(
                context = getApplication(),
                onTextRecognized = { _, _ -> },
                onPartialTranscript = {},
                onStatusChanged = {},
                onErrorOccurred = {}
            )
            standalone.downloadModel(variant, viewModelScope)
            viewModelScope.launch {
                standalone.modelState.collect { state ->
                    _fallbackVoskState.value = state
                    if (state is VoskManager.ModelState.Ready) {
                        refreshInstalledStatus()
                        updateSettings(settings.value.copy(selectedVoskModel = variant))
                    }
                }
            }
        }
    }

    fun selectModel(variant: VoskModelVariant) {
        updateSettings(settings.value.copy(selectedVoskModel = variant))
        VoiceCaptureService.voskManagerInstance?.loadModel(variant)
    }

    fun toggleGrammar(enabled: Boolean) {
        updateSettings(settings.value.copy(useGrammarConstraint = enabled))
        val activeRules = rules.value.filter { it.isEnabled }
        VoiceCaptureService.voskManagerInstance?.updateGrammar(
            rules = activeRules,
            wakeWord = settings.value.wakeWord,
            enabled = enabled
        )
    }

    fun toggleService() {
        if (serviceState.value.isRunning) {
            VoiceCaptureService.stopService(getApplication())
        } else {
            VoiceCaptureService.startService(getApplication())
        }
    }

    fun startService() {
        VoiceCaptureService.startService(getApplication())
    }

    fun stopService() {
        VoiceCaptureService.stopService(getApplication())
    }

    fun dispatchNow() {
        VoiceCaptureService.dispatchBufferedCommandNow(getApplication())
    }

    fun updateSettings(newSettings: AppSettings) {
        prefs.updateSettings(newSettings)
    }

    fun saveRule(rule: VoiceRuleEntity) {
        viewModelScope.launch {
            if (rule.id == 0L) {
                db.voiceRuleDao().insertRule(rule)
            } else {
                db.voiceRuleDao().updateRule(rule)
            }
            delay(100)
            val activeRules = db.voiceRuleDao().getActiveRules()
            VoiceCaptureService.voskManagerInstance?.updateGrammar(
                rules = activeRules,
                wakeWord = settings.value.wakeWord,
                enabled = settings.value.useGrammarConstraint
            )
        }
    }

    fun toggleRuleEnabled(rule: VoiceRuleEntity) {
        viewModelScope.launch {
            db.voiceRuleDao().updateRule(rule.copy(isEnabled = !rule.isEnabled))
            delay(100)
            val activeRules = db.voiceRuleDao().getActiveRules()
            VoiceCaptureService.voskManagerInstance?.updateGrammar(
                rules = activeRules,
                wakeWord = settings.value.wakeWord,
                enabled = settings.value.useGrammarConstraint
            )
        }
    }

    fun deleteRule(rule: VoiceRuleEntity) {
        viewModelScope.launch {
            db.voiceRuleDao().deleteRule(rule)
            delay(100)
            val activeRules = db.voiceRuleDao().getActiveRules()
            VoiceCaptureService.voskManagerInstance?.updateGrammar(
                rules = activeRules,
                wakeWord = settings.value.wakeWord,
                enabled = settings.value.useGrammarConstraint
            )
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            db.commandLogDao().clearLogs()
        }
    }

    fun simulateVoiceCommand(text: String) {
        if (serviceState.value.isRunning) {
            VoiceCaptureService.simulateVoiceCommand(getApplication(), text)
        } else {
            VoiceCaptureService.startService(getApplication())
            viewModelScope.launch {
                delay(400)
                VoiceCaptureService.simulateVoiceCommand(getApplication(), text)
            }
        }
    }

    fun resendLogIntent(log: CommandLogEntity) {
        val app = getApplication<Application>()
        try {
            val intent = Intent(log.intentAction).apply {
                flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES or Intent.FLAG_RECEIVER_FOREGROUND
                putExtra("voice_command", log.rawText)
                putExtra("command", log.rawText)
                putExtra("%voice_command", log.rawText)
                putExtra("%voice_text", log.rawText)
                putExtra("timestamp", System.currentTimeMillis())
            }
            app.sendBroadcast(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun testTts(text: String) {
        val current = settings.value
        ttsTester.speak(text, current)
    }

    override fun onCleared() {
        super.onCleared()
        ttsTester.shutdown()
    }
}
