package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppSettings
import com.example.data.model.OfflineEngineMode
import com.example.data.model.VoskModelVariant
import com.example.voice.VoskManager

@Composable
fun OfflineEngineScreen(
    settings: AppSettings,
    voskState: VoskManager.ModelState,
    isLGraphInstalled: Boolean,
    isSmallInstalled: Boolean,
    onDownloadModel: (VoskModelVariant) -> Unit,
    onSelectModel: (VoskModelVariant) -> Unit,
    onToggleGrammar: (Boolean) -> Unit,
    onUpdateSettings: (AppSettings) -> Unit,
    onTestTts: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Vosk English Models Management Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Hearing,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "English Vosk STT Models",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (voskState) {
                                is VoskManager.ModelState.Ready -> Color(0xFF00E676).copy(alpha = 0.2f)
                                is VoskManager.ModelState.Downloading -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                text = when (voskState) {
                                    is VoskManager.ModelState.Ready -> "ACTIVE: ${voskState.variant.displayName}"
                                    is VoskManager.ModelState.Downloading -> "DOWNLOADING ${voskState.progressPercent}%"
                                    is VoskManager.ModelState.Error -> "ERROR"
                                    else -> "NOT LOADED"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (voskState) {
                                    is VoskManager.ModelState.Ready -> Color(0xFF00E676)
                                    is VoskManager.ModelState.Downloading -> MaterialTheme.colorScheme.onPrimaryContainer
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Vosk provides Kaldi acoustic speech recognition 100% offline. Compare the two English engines below to solve low recognition accuracy:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 1. High Accuracy Model Option
                    OutlinedCard(
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (settings.selectedVoskModel == VoskModelVariant.VOSK_HIGH_ACCURACY_LGRAPH)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Vosk English High-Accuracy LGraph",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(
                                                text = "BEST",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Size: ~128 MB • Model: vosk-model-en-us-0.22-lgraph",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Kaldi Lookahead Graph provides drastically lower Word Error Rate (WER). Outstanding at differentiating similar-sounding English command verbs.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isLGraphInstalled) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Installed", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00E676))
                                    }
                                    if (settings.selectedVoskModel != VoskModelVariant.VOSK_HIGH_ACCURACY_LGRAPH) {
                                        Button(
                                            onClick = { onSelectModel(VoskModelVariant.VOSK_HIGH_ACCURACY_LGRAPH) },
                                            modifier = Modifier.testTag("switch_to_lgraph_btn")
                                        ) {
                                            Text("Switch to this Model")
                                        }
                                    } else {
                                        Text("Current Active Model", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                } else {
                                    Button(
                                        onClick = { onDownloadModel(VoskModelVariant.VOSK_HIGH_ACCURACY_LGRAPH) },
                                        modifier = Modifier.fillMaxWidth().testTag("download_lgraph_engine_btn")
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Download High-Accuracy English (~128MB)")
                                    }
                                }
                            }
                        }
                    }

                    // 2. Small Model Option
                    OutlinedCard(
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (settings.selectedVoskModel == VoskModelVariant.VOSK_SMALL_EN)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Column {
                                Text(
                                    text = "Vosk English Small (Compact)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Size: ~40 MB • Model: vosk-model-small-en-us-0.15",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Quantized for low memory. Prone to lower accuracy in open dictation, but works well when Command Grammar is enabled.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSmallInstalled) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Installed", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00E676))
                                    }
                                    if (settings.selectedVoskModel != VoskModelVariant.VOSK_SMALL_EN) {
                                        Button(
                                            onClick = { onSelectModel(VoskModelVariant.VOSK_SMALL_EN) },
                                            modifier = Modifier.testTag("switch_to_small_btn")
                                        ) {
                                            Text("Switch to this Model")
                                        }
                                    } else {
                                        Text("Current Active Model", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                } else {
                                    Button(
                                        onClick = { onDownloadModel(VoskModelVariant.VOSK_SMALL_EN) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        modifier = Modifier.fillMaxWidth().testTag("download_small_engine_btn")
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Download Small English (~40MB)")
                                    }
                                }
                            }
                        }
                    }

                    // Progress indicator if downloading
                    if (voskState is VoskManager.ModelState.Downloading) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Downloading & extracting English model (${voskState.progressPercent}%)...", style = MaterialTheme.typography.bodySmall)
                            LinearProgressIndicator(
                                progress = { voskState.progressPercent / 100f },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Vosk Grammar Constraint Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Command Grammar Constraint",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Switch(
                            checked = settings.useGrammarConstraint,
                            onCheckedChange = onToggleGrammar,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("grammar_constraint_switch")
                        )
                    }

                    Text(
                        text = if (settings.useGrammarConstraint)
                            "ACTIVE: Vosk is constrained to your automation rules and English action words (e.g. 'flashlight', 'lights on', 'volume'). This increases English voice accuracy to >95% and avoids acoustic confusion."
                        else
                            "INACTIVE (Open Vocabulary): Vosk searches the entire English dictionary. Accuracy is lower for commands because words can be confused with random conversational terms.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Recognition Architecture Selection
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Speech Recognition Architecture",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OfflineEngineMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (settings.engineMode == mode)
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    RoundedCornerShape(8.dp)
                                )
                                .selectable(
                                    selected = settings.engineMode == mode,
                                    onClick = { onUpdateSettings(settings.copy(engineMode = mode)) }
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.engineMode == mode,
                                onClick = { onUpdateSettings(settings.copy(engineMode = mode)) }
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = mode.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = mode.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Acoustic Noise Suppression & VAD Tuning
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Microphone & Acoustic VAD Tuning",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hardware Noise Suppression", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Reduces background room noise, fans, and echo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = settings.noiseSuppressionEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(noiseSuppressionEnabled = it)) }
                        )
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("VAD Voice Detection Sensitivity", style = MaterialTheme.typography.bodySmall)
                            Text("${(settings.vadSensitivity * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = settings.vadSensitivity,
                            onValueChange = { onUpdateSettings(settings.copy(vadSensitivity = it)) },
                            valueRange = 0.1f..1.0f
                        )
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Silence Pause Before Triggering", style = MaterialTheme.typography.bodySmall)
                            Text("${settings.silenceTimeoutMs} ms", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = settings.silenceTimeoutMs.toFloat(),
                            onValueChange = { onUpdateSettings(settings.copy(silenceTimeoutMs = it.toLong())) },
                            valueRange = 400f..2000f,
                            steps = 15
                        )
                    }
                }
            }
        }

        // Command Completion Delay & Phrase Stitching Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Command Completion Delay",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${settings.commandDelayMs / 1000f}s DELAY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Customize how long VoiceBridge waits in silence before considering your words as a completed command. Prevents cutting off sentences when speaking slowly or pausing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Completion Wait Time (Pause Delay)", style = MaterialTheme.typography.bodySmall)
                            Text("${settings.commandDelayMs / 1000f} seconds (${settings.commandDelayMs} ms)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = settings.commandDelayMs.toFloat(),
                            onValueChange = { onUpdateSettings(settings.copy(commandDelayMs = it.toLong())) },
                            valueRange = 500f..4000f,
                            steps = 13,
                            modifier = Modifier.testTag("engine_delay_slider")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Multi-Segment Speech Stitching", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Combines consecutive voice fragments spoken within the pause window into a single complete sentence", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = settings.stitchMultiSegmentSpeech,
                            onCheckedChange = { onUpdateSettings(settings.copy(stitchMultiSegmentSpeech = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("engine_stitch_switch")
                        )
                    }
                }
            }
        }

        // Voice Synthesis (TTS) - Natural Male Voice
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Speech Synthesis (Male Voice)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Natural tone • Male English voice",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Switch(
                            checked = settings.ttsFeedbackEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(ttsFeedbackEnabled = it)) }
                        )
                    }

                    if (settings.ttsFeedbackEnabled) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Voice tone is set to natural pitch (1.0x) using English Male voice profiles.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Speech Rate (Speed)", style = MaterialTheme.typography.bodySmall)
                                Text("${(settings.ttsSpeechRate * 10).toInt() / 10f}x", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = settings.ttsSpeechRate,
                                onValueChange = { onUpdateSettings(settings.copy(ttsSpeechRate = it)) },
                                valueRange = 0.8f..1.3f
                            )
                        }

                        OutlinedTextField(
                            value = settings.ttsTemplate,
                            onValueChange = { onUpdateSettings(settings.copy(ttsTemplate = it)) },
                            label = { Text("Spoken Feedback Template") },
                            placeholder = { Text("Command received: %s") },
                            supportingText = { Text("'%s' is replaced with recognized English command") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                val sample = String.format(settings.ttsTemplate, "flashlight activated")
                                onTestTts(sample)
                            },
                            modifier = Modifier.fillMaxWidth().testTag("test_voice_feedback_button")
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Test Male Voice Feedback")
                        }
                    }
                }
            }
        }

        // Start on Boot
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Start Service On Device Boot",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Automatically activate background microphone listening when Android completes booting.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Switch(
                        checked = settings.startOnBoot,
                        onCheckedChange = { onUpdateSettings(settings.copy(startOnBoot = it)) }
                    )
                }
            }
        }
    }
}
