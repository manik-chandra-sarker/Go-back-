package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SettingsPreferences
import com.example.data.db.AppDatabase
import com.example.data.db.VoiceRuleEntity
import com.example.data.model.MatchMode
import com.example.data.model.OfflineEngineMode
import com.example.data.model.TargetApp
import com.example.data.model.VoskModelVariant
import com.example.voice.OfflineCommandMatcher
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("VoiceBridge", appName)
    }

    @Test
    fun `preferences default settings validation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = SettingsPreferences.getInstance(context)
        val settings = prefs.getSettings()

        assertTrue(settings.preferOffline)
        assertEquals(OfflineEngineMode.GOOGLE_ON_DEVICE, settings.engineMode)
        assertEquals(VoskModelVariant.VOSK_HIGH_ACCURACY_LGRAPH, settings.selectedVoskModel)
        org.junit.Assert.assertFalse(settings.useGrammarConstraint)
        assertEquals(1500L, settings.commandDelayMs)
        assertTrue(settings.stitchMultiSegmentSpeech)
        assertEquals(TargetApp.TASKER, settings.defaultTarget)
        assertEquals("net.dinglisch.android.tasker.ACTION_SPEECH_COMMAND", settings.defaultAction)
        assertEquals("voice_command", settings.defaultExtraKey)
    }

    @Test
    fun `offline acoustic command matcher evaluation`() {
        val matcher = OfflineCommandMatcher()
        val rules = listOf(
            VoiceRuleEntity(
                name = "Flashlight",
                triggerPhrase = "flashlight",
                matchMode = MatchMode.CONTAINS,
                targetApp = TargetApp.TASKER
            ),
            VoiceRuleEntity(
                name = "Good Morning Routine",
                triggerPhrase = "good morning routine",
                matchMode = MatchMode.CONTAINS,
                targetApp = TargetApp.MACRODROID
            )
        )

        val shortWordSamples = ShortArray(7200) { (it % 1000).toShort() }
        val shortResult = matcher.matchUtterance(shortWordSamples, 450L, rules)
        assertNotNull(shortResult.matchedPhrase)

        val longPhraseSamples = ShortArray(21600) { (it % 1500).toShort() }
        val longResult = matcher.matchUtterance(longPhraseSamples, 1350L, rules)
        assertNotNull(longResult.matchedPhrase)
    }

    @Test
    fun `database rule insert and retrieval`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val dao = db.voiceRuleDao()

        val rule = VoiceRuleEntity(
            name = "Test Flashlight",
            triggerPhrase = "flashlight on",
            matchMode = MatchMode.CONTAINS,
            targetApp = TargetApp.TASKER,
            customAction = "net.dinglisch.android.tasker.ACTION_SPEECH_COMMAND",
            customExtraKey = "command",
            customPayload = "torch_1",
            isEnabled = true
        )

        val id = dao.insertRule(rule)
        assertTrue(id > 0)
        val activeRules = dao.getActiveRules()
        assertNotNull(activeRules.find { it.triggerPhrase == "flashlight on" })
    }
}
