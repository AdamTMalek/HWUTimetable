package com.github.hwutimetable.updater

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.os.PersistableBundle
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
internal class UpdateManager(private val context: Context) {
    private val logTag = "UpdateManager"
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val jobId = 1
    private val settings = Settings()
    private val jobScheduler = context.getSystemService(JobScheduler::class.java)!!

    /**
     * Storage for alarm settings.
     */
    private inner class Settings {
        fun isUpdatingEnabled() = sharedPreferences.getBoolean(context.getString(R.string.updates_pref_key), true)

        fun canUseMobileData() = sharedPreferences.getBoolean(context.getString(R.string.data_pref_key), false)

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

    private fun getRequiredNetworkType() = if (settings.canUseMobileData())
        JobInfo.NETWORK_TYPE_ANY
    else
        JobInfo.NETWORK_TYPE_UNMETERED

    private fun getJobInfoBuilder() = with(JobInfo.Builder(jobId, ComponentName(context, UpdateService::class.java))) {
        setRequiredNetworkType(getRequiredNetworkType())
        setPersisted(true)
    }

    /**
     * The alarm will be automatically set whenever the preferences change.
     * This means, there is no need to call this function.
     */
    fun setAlarm() {
        if (settings.isUpdatingEnabled()) {
            val currentTime = Instant.now().millis
            val triggerAt = settings.getUpdateTimeInMillis()

            val jobExtras = PersistableBundle().apply {
                putBoolean(UpdateService.SCHEDULE_NEXT_EXTRA, true)
            }

            val jobInfo = with(getJobInfoBuilder()) {
                setMinimumLatency(triggerAt - currentTime)
                setExtras(jobExtras)
                build()
            }

            scheduleJob(jobInfo)
        } else {
            jobScheduler.cancel(jobId)
            Log.d(logTag, "Alarm disabled.")
        }
    }

    fun setPeriodicAlarm() {
        val jobInfo = with(getJobInfoBuilder()) {
            setPeriodic(settings.getInterval())
            build()
        }

        scheduleJob(jobInfo)
    }

    private fun scheduleJob(jobInfo: JobInfo) {
        jobScheduler.schedule(jobInfo)
        Log.i(logTag, getScheduledLogMessage(jobInfo))
    }

    private fun getScheduledLogMessage(jobInfo: JobInfo): String {
        return if (jobInfo.isPeriodic)
            "Periodic job scheduled. Will run every ${settings.getInterval()}ms"
        else
            "Job scheduled. Will run in ${jobInfo.minLatencyMillis}ms"
    }
}
