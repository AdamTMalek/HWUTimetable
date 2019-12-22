package com.example.hwutimetable.updater

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import com.example.hwutimetable.R
import java.util.*

internal class UpdateManager(private val context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {
    private val logTag = "UpdateManager"
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val updaterIntent: PendingIntent
    private val settings = Settings()

    internal class Settings {
        var enabled: Boolean = false
        var interval: Long = 0
        var updateTimeInMillis: Long = 0L
    }

    init {
        updaterIntent = Intent(context, UpdateService::class.java).let { intent ->
            PendingIntent.getService(context, 0, intent, 0)
        }

        setAlarm()
    }

    private fun setAlarm() {
        if (settings.enabled) {
            Log.d(logTag, "Enabling the alarm")
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                settings.updateTimeInMillis,
                settings.interval,
                updaterIntent
            )
        } else {
            Log.d(logTag, "Disabling the alarm")
            alarmManager.cancel(updaterIntent)
        }
    }

    private fun setEnabled() {
        settings.enabled = sharedPreferences.getBoolean(context.getString(R.string.updates_pref_key), false)
    }

    private fun setTime() {
        val minutesAfterMidnight = sharedPreferences.getInt(context.getString(R.string.time_pref_key), 0)

        val updateTimeInMillis = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, minutesAfterMidnight / 60)
            set(Calendar.MINUTE, minutesAfterMidnight % 60)
        }.timeInMillis

        settings.updateTimeInMillis = updateTimeInMillis
    }

    private fun setFrequency() {
        val daysBetweenUpdate = sharedPreferences.getInt(context.getString(R.string.frequency_pref_key), 0)
        settings.interval = daysBetweenUpdate * 24 * 60 * 60 * 1000L
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            context.getString(R.string.updates_pref_key) -> setEnabled()
            context.getString(R.string.time_pref_key) -> setTime()
            context.getString(R.string.frequency_pref_key) -> setFrequency()
        }

        setAlarm()
    }
}