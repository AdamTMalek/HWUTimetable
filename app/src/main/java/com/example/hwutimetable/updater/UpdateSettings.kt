package com.example.hwutimetable.updater

import android.net.NetworkCapabilities.TRANSPORT_WIFI
import com.google.gson.Gson
import org.joda.time.LocalTime
import java.io.File

class UpdateSettings(private val filesDirectory: File) {
    private var settings: Settings? = null
    private val filename = "update_settings.json"

    fun getSettings(): Settings {
        // If, during the lifetime of the object the Settings were initialised
        // return the local copy
        if (settings != null)
            return settings as Settings

        // Otherwise, load settings from the file
        settings = loadSettingsFromFile()

        if (settings == null)  // If file does not exist
            settings = loadDefaultSettings()

        return settings as Settings
    }

    private fun loadSettingsFromFile(): Settings? {
        val file = File(filesDirectory, filename)
        if (!file.exists())
            return null

        return Gson().fromJson(file.readText(), Settings::class.java)
    }

    private fun loadDefaultSettings(): Settings {
        return Settings(mutableListOf(TRANSPORT_WIFI), LocalTime.MIDNIGHT, 1)
    }

    fun saveSettings(settings: Settings) {
        val file = File(filesDirectory, filename)
        val json = Gson().toJson(settings, Settings::class.java)

        file.writeText(json)
    }

    class Settings(
        var transportMethods: MutableList<Int>,
        var updateTime: LocalTime,
        var updateFrequency: Int
    )
}