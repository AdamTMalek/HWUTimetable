package com.github.hwutimetable.updater

import android.app.Notification
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.hwutimetable.NotificationChannelManager
import com.github.hwutimetable.R
import com.github.hwutimetable.parser.Timetable


/**
 * This class is responsible for creating and delivering (i.e. displaying) notifications
 * about ongoing and/or finished update process.
 */
internal class UpdateNotifier(val context: Context) : OnUpdateInProgressListener, OnUpdateFinishedListener {
    companion object {
        const val LOG_TAG = "update_notifier"
        const val CHANNEL_ID = "update_notifications"  // Notification channel ID
        private const val IN_PROGRESS_ID = 0  // ID of the "in-progress" notification.
        private const val POST_UPDATE_ID = 1  // ID of the "update finished" notification
    }

    init {
        if (!NotificationChannelManager.checkNotificationChannelExists(context, CHANNEL_ID))
            NotificationChannelManager.createNotificationChannel(context, CHANNEL_ID)
    }

    /**
     * Update in progress notification is a notification with an indeterminate progress bar
     * informing the user about ongoing update process
     */
    private fun createUpdateInProgressNotification(): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentTitle("Timetable Update")
            .setContentText("Update in progress")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setProgress(0, 0, true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).build()
    }

    /**
     * Create a notification that will be displayed to the user after the update process has finished
     * @param updated: Number of timetables that got updated
     */
    private fun createPostUpdateNotification(updated: Int): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentTitle("Timetable Update")
            .setContentText("Updated $updated timetable(s)")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH).build()
    }

    /**
     * Creates and shows "update in progress" notification
     */
    override fun onUpdateInProgress() {
        Log.d(LOG_TAG, "Update in progress")
        val notification = createUpdateInProgressNotification()
        showNotification(IN_PROGRESS_ID, notification)
    }

    /**
     * This method will be invoked by the [Updater] after it has finished updating.
     * It will check if there were any timetables updated (the [updated] collection may be empty),
     * and if it's not empty - it will show create and show post-update notification.
     */
    override fun onUpdateFinished(updated: Collection<Timetable.Info>) {
        cancelInProgressNotification()

        if (updated.isEmpty()) {
            Log.d(LOG_TAG, "Update finished. All timetables are up-to-date.")
            return
        }

        val notification = createPostUpdateNotification(updated.size)
        showNotification(POST_UPDATE_ID, notification)
    }

    private fun showNotification(id: Int, notification: Notification) {
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    private fun cancelInProgressNotification() {
        NotificationManagerCompat.from(context).cancel(IN_PROGRESS_ID)
    }
}