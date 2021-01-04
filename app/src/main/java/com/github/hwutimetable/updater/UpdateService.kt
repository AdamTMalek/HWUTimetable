package com.github.hwutimetable.updater

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.hwutimetable.parser.Timetable

/**
 * [UpdateService] is a service that is responsible for starting the update process the timetables stored on the device
 * (using [Updater]), even if the app is not currently open. The service will register another notification
 * receiver (i.e. [OnUpdateFinishedListener]) if it is specified in the intent extras. This extra receiver
 * may be used to create Android notifications informing the user about ongoing update or the result of it.
 */
class UpdateService(appContext: Context, params: WorkerParameters) : Worker(appContext, params),
    OnUpdateFinishedListener {
    companion object {
        const val LOG_TAG = "UpdateService"
        const val SCHEDULE_NEXT_EXTRA = "ScheduleNext"
    }

    private val updater by lazy { Updater(applicationContext.filesDir, applicationContext) }

    /**
     * Register this object as an [OnUpdateFinishedListener] to the [updater]
     */
    private fun registerSelfAsReceiver() {
        updater.addFinishedListener(this)
    }

    override fun doWork(): Result {
        Log.i(LOG_TAG, "Update service started.")
        configureNotifier()
        registerSelfAsReceiver()
        updater.update()

        return Result.success()
    }

    private fun configureNotifier() {
        val notifier = getDefaultNotifier()
        updater.addInProgressListener(notifier as OnUpdateInProgressListener)
        updater.addFinishedListener(notifier as OnUpdateFinishedListener)
    }

    private fun getDefaultNotifier() = UpdateNotifier(applicationContext)

    /**
     * Post-update callback received from the [updater]
     */
    override fun onUpdateFinished(updated: Collection<Timetable.Info>) {
        val logMessage = if (updated.isEmpty())
            "Post-Update callback received but no timetables were updated"
        else
            "Post-Update callback received. Updater updated $updated timetables"

        Log.i(LOG_TAG, logMessage)
    }
}
