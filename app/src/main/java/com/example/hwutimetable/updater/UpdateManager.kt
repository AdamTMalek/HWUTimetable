package com.example.hwutimetable.updater

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import org.joda.time.LocalTime
import java.util.*


/**
 * Update manager is the main interface that is used to set the repeating
 * timetable update checks. Use it to change the settings of the alarm and to set it.
 */
class UpdateManager(val context: Context) {
    val settings: UpdateSettings.Settings = UpdateSettings(context.filesDir).getSettings()

    /**
     * The [UpdatePerformer] performing the updating process will check the transport method
     * before starting to update the timetables. This method will add the transport method
     * to the settings to allow updating process use that method to perform updating.
     * @param transportMethod: Constant from [android.net.NetworkCapabilities]
     */
    fun addTransportMethod(transportMethod: Int) {
        settings.transportMethods.add(transportMethod)
    }

    /**
     * Delete transport method from the allowed methods.
     */
    fun removeTransportMethod(transportMethod: Int) {
        settings.transportMethods.remove(transportMethod)
    }

    /**
     * Set the time at which the updating process should start.
     */
    @Throws(IllegalArgumentException::class)
    fun setUpdateTime(time: LocalTime) {
        settings.updateTime = time
    }

    /**
     * Set the frequency as the number of days between each update check.
     */
    @Throws(IllegalArgumentException::class)
    fun setUpdateFrequency(days: Int) {
        settings.updateFrequency = days
    }

    /**
     * Set the update alarm based on the currently set settings
     */
    fun setUpdateAlarm() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, UpdateService::class.java).let { intent ->
            PendingIntent.getService(context, 0, intent, 0)
        }

        // Cancel old alarms
        alarmManager.cancel(intent)

        // Set up new alarm
        val updateHourInMillis = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, settings.updateTime.hourOfDay)
            set(Calendar.MINUTE, settings.updateTime.minuteOfHour)
        }.timeInMillis

        // Day to milliseconds
        val updateInterval = settings.updateFrequency * 24L * 60L * 60L * 1000L

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            updateHourInMillis,
            updateInterval,
            intent
        )
    }
}