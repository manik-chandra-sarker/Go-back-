package com.example

import android.app.Application
import com.example.data.SettingsPreferences
import com.example.data.db.AppDatabase

class VoiceBridgeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDatabase.getInstance(this)
        SettingsPreferences.getInstance(this)
    }
}
