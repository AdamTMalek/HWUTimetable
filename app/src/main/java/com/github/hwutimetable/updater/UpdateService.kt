package com.github.hwutimetable.updater

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import com.github.hwutimetable.parser.Timetable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * [UpdateService] is a service that is responsible for starting the update process the timetables stored on the device
 * (using [Updater]), even if the app is not currently open. The service will register another notification
 * receiver (i.e. [OnUpdateFinishedListener]) if it is specified in the intent extras. This extra receiver
 * may be used to create Android notifications informing the user about ongoing update or the result of it.
 */
class UpdateService : JobService(), OnUpdateFinishedListener {
    companion object {
        const val LOG_TAG = "UpdateService"
        const val SCHEDULE_NEXT_EXTRA = "ScheduleNext"
    }

    private lateinit var updateJob: Job
    private val updater by lazy { Updater(this.filesDir, this) }

    /**
     * Register this object as an [OnUpdateFinishedListener] to the [updater]
     */
    private fun registerSelfAsReceiver() {
        updater.addFinishedListener(this)
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.i(LOG_TAG, "Update service started.")
        updateJob = CoroutineScope(Dispatchers.IO).launch {
            configureNotifier(params)
            registerSelfAsReceiver()
            updater.update()
            jobFinished(params, false)  // Inform the system that the job is finished
        }

        val shouldScheduleNextJob = params?.extras?.getBoolean(SCHEDULE_NEXT_EXTRA) ?: false
        if (shouldScheduleNextJob) {
            val updateManager = UpdateManager(applicationContext)
            updateManager.setPeriodicAlarm()
        }

        return true // Indicates to the system that there's another thread running. Job is not finished yet.
    }

    private fun configureNotifier(params: JobParameters?) {
        if (params == null) {
            updater.addFinishedListener(getDefaultNotifier())
            return
        }

        val notifier = params.extras.get("notifier") ?: getDefaultNotifier()
        updater.addInProgressListener(notifier as OnUpdateInProgressListener)
        updater.addFinishedListener(notifier as OnUpdateFinishedListener)
    }

    private fun getDefaultNotifier() = UpdateNotifier(this)

    /**
     * Post-update callback received from the [updater]
     */
    override fun onUpdateFinished(updated: Collection<Timetable.Info>) {
        val logMessage = if (updated.isEmpty())
            "Post-Update callback received but no timetables were updated"
        else
            "Post-Update callback received. Updater updated $updated timetables"

        Log.i(LOG_TAG, logMessage)
        stopSelf()
    }

    /**
     * Called by the system if the job is cancelled before it's finished.
     */
    override fun onStopJob(params: JobParameters?): Boolean {
        updateJob.cancel()
        return true  // We want the system to reschedule the job
    }
}
