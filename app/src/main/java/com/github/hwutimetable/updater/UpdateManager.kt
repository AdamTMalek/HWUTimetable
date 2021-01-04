package com.github.hwutimetable.updater

import android.app.job.JobInfo
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.work.*
import com.github.hwutimetable.R
import org.joda.time.DateTimeFieldType
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import java.util.concurrent.TimeUnit


/**
 * UpdateManager is a class responsible for enabling and disabling the alarm based on the preferences
 * that are set in the settings. The class itself implements [SharedPreferences.OnSharedPreferenceChangeListener]
 * therefore it will automatically be informed of any preference change and react appropriately.
 */

internal class UpdateManager(private val context: Context) {
    private val logTag = "UpdateManager"
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val updateWorkName = "update-work"
    private val settings = Settings()
    private val workManager = WorkManager.getInstance(context)

    /**
     * Storage for alarm settings.
     */
    private inner class Settings {
        fun isUpdatingEnabled() = sharedPreferences.getBoolean(context.getString(R.string.updates_pref_key), true)

        fun canUseMobileData() = sharedPreferences.getBoolean(context.getString(R.string.data_pref_key), false)

        fun getIntervalInDays(): Int {
            return sharedPreferences.getInt(context.getString(R.string.frequency_pref_key), 1)
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

    private fun getRequiredNetworkType() = if (settings.canUseMobileData())
        JobInfo.NETWORK_TYPE_ANY
    else
        JobInfo.NETWORK_TYPE_UNMETERED

    private fun getJobInfo(jobId: Int) =
        with(JobInfo.Builder(jobId, ComponentName(context, UpdateService::class.java))) {
            setRequiredNetworkType(getRequiredNetworkType())
            setPersisted(true)
        }

    /**
     * The alarm will be automatically set whenever the preferences change.
     * This means, there is no need to call this function.
     */
    fun setAlarm() {
        if (settings.isUpdatingEnabled()) {
            val workRequest = constructWorkRequest()
            enqueueWork(workRequest)
        } else {
            dequeueWork()
        }
    }

    private fun constructWorkRequest(): PeriodicWorkRequest {
        val interval = settings.getIntervalInDays().toLong()
        val delay = getInitialWorkDelayInMillis()

        return PeriodicWorkRequestBuilder<UpdateService>(interval, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(getWorkConstraints())
            .build()
    }

    private fun getInitialWorkDelayInMillis(): Long {
        val now = Instant.now().millis
        val updateTime = settings.getUpdateTimeInMillis()
        return updateTime - now
    }

    private fun getWorkConstraints(): Constraints {
        return with(Constraints.Builder()) {
            if (settings.canUseMobileData())
                setRequiredNetworkType(NetworkType.CONNECTED)
            else
                setRequiredNetworkType(NetworkType.UNMETERED)

            build()
        }
    }

    private fun enqueueWork(workRequest: PeriodicWorkRequest) {
        workManager.enqueueUniquePeriodicWork(updateWorkName, ExistingPeriodicWorkPolicy.REPLACE, workRequest)
        Log.i(logTag, "Update work has been enqueued.")
    }

    private fun dequeueWork() {
        workManager.cancelUniqueWork(updateWorkName)
        Log.i(logTag, "Update work has been dequeued.")
    }
}
