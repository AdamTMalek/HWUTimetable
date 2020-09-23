package com.github.hwutimetable.updater

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.util.Log
import androidx.preference.PreferenceManager
import com.github.hwutimetable.R
import org.joda.time.DateTimeFieldType
import org.joda.time.DateTimeZone
import org.joda.time.Instant


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
    private val updaterIntent: PendingIntent = Intent(context, UpdateService::class.java).let { intent ->
        PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }
    private val settings = Settings()

    /**
     * Storage for alarm settings.
     */
    private inner class Settings {
        fun isUpdatingEnabled() = sharedPreferences.getBoolean(context.getString(R.string.updates_pref_key), true)

        fun getInterval(): Long {
            val daysBetweenUpdate = sharedPreferences.getInt(context.getString(R.string.frequency_pref_key), 1)
            return daysBetweenUpdate * 24 * 60 * 60 * 1000L
        }

        fun getUpdateTimeInMillis(): Long {
            val minutesAfterMidnight = sharedPreferences.getInt(context.getString(R.string.time_pref_key), 0)
            return getTriggerTimeInMillis(minutesAfterMidnight)
        }

        /**
         * This method will return the trigger time of the alarm in milliseconds.
         * As the documentation of the alarms says, if we specify the trigger time that is in the past
         * the alarm will be triggered immediately.
         * Therefore, this method will check if the specified [minutesAfterMidnight] is in the past or not.
         * If it is in the past, it will return the time for the next day, otherwise the time will be for the
         * current day.
         *
         * **See Also:** [Alarm Documentation](https://developer.android.com/training/scheduling/alarms.html#set)
         */
        private fun getTriggerTimeInMillis(minutesAfterMidnight: Int): Long {
            val hourOfDay = minutesAfterMidnight / 60
            val minuteOfHour = minutesAfterMidnight % 60

            val now = Instant.now()
            var date = now.toDateTime()
                .withField(DateTimeFieldType.hourOfDay(), hourOfDay)
                .withField(DateTimeFieldType.minuteOfHour(), minuteOfHour)

            if (date.isBefore(now)) {
                date = date.plusDays(1)
            }

            return date.toDateTime(DateTimeZone.UTC).millis
        }
    }

    /**
     * The alarm will be automatically set whenever the preferences change.
     * This means, there is no need to call this function.
     */
    fun setAlarm() {
        if (settings.isUpdatingEnabled()) {
            val triggerAt = settings.getUpdateTimeInMillis()
            val interval = settings.getInterval()

            enableBootReceiver()
            Log.d(logTag, "Enabling the alarm")
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                interval,
                updaterIntent
            )
        } else {
            disableBootReceiver()
            Log.d(logTag, "Disabling the alarm")
            alarmManager.cancel(updaterIntent)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        setAlarm()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
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
