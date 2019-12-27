package com.example.hwutimetable.updater

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.util.Log
import androidx.preference.PreferenceManager
import com.example.hwutimetable.R
import java.util.*


/**
 * UpdateManager is a class responsible for enabling and disabling the alarm based on the preferences
 * that are set in the settings. The class itself implements [SharedPreferences.OnSharedPreferenceChangeListener]
 * therefore it will automatically be informed of any preference change and react appropriately.
 */
internal class UpdateManager(private val context: Context) :
    BroadcastReceiver(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val logTag = "UpdateManager"
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val updaterIntent: PendingIntent  // UpdateService intent
    private val settings = Settings()

    /**
     * Storage for alarm settings. Note [interval] and [updateTimeInMillis] are different than the
     * stored preferences.
     */
    private inner class Settings {
        val enabled: Boolean
            get() = sharedPreferences.getBoolean(context.getString(R.string.updates_pref_key), false)

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
            enableBootReceiver()
            Log.d(logTag, "Enabling the alarm")
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                settings.updateTimeInMillis,
                settings.interval,
                updaterIntent
            )
        } else {
            disableBootReceiver()
            Log.d(logTag, "Disabling the alarm")
            alarmManager.cancel(updaterIntent)
        }
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
            context.getString(R.string.time_pref_key) -> setTime()
            context.getString(R.string.frequency_pref_key) -> setFrequency()
        }

        setAlarm()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            setTime()
            setFrequency()
            setAlarm()
        }
    }

    /**
     * Enables this receiver to receive BOOT_COMPLETED intents which are disabled by default
     */
    private fun enableBootReceiver() {
        val receiver = ComponentName(context, this::class.java)
        context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    /**
     * Disable this BOOT_COMPLETED receiver
     */
    private fun disableBootReceiver() {
        val receiver = ComponentName(context, this::class.java)
        context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}
