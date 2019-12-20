package com.example.hwutimetable.updater

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.hwutimetable.R
import com.example.hwutimetable.filehandler.TimetableInfo

/**
 * [UpdateService] is a service that is responsible for updating the timetables stored on the device
 * (using [Updater]), even if the app is not currently open. The service will send a notification
 * after the [updater] has updated the timetables. If no timetables were updated then no notification
 * will be sent.
 */
class UpdateService : Service(), UpdateNotificationReceiver {
    private val logTag = "UpdateService"
    private val notificationChannelId = "update_notifications"
    private val updater: UpdatePerformer = Updater(this.filesDir)
    private lateinit var settings: UpdateSettings.Settings

    override fun onCreate() {
        settings = UpdateSettings(this.filesDir).getSettings()

        if (checkNotificationChannelExists())
            createNotificationChannel()

        registerSelfAsReceiver()
    }

    /**
     * Register this object as an [UpdateNotificationReceiver] to the [updater]
     */
    private fun registerSelfAsReceiver() {
        updater.addNotificationReceiver(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        updater.update()
        return START_STICKY
    }

    /**
     * Check if the notification channel exists (applicable for Android O and later).
     * For older Android versions, the method returns true
     */
    private fun checkNotificationChannelExists(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return TextUtils.isEmpty(notificationChannelId).not()
        }
        return true
    }

    /**
     * (For Android O and later) create a notification channel if it does not exist
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.update_notification_channel_name)
            val desc = getString(R.string.update_notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(notificationChannelId, name, importance)
            channel.description = desc
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Post-update callback received from the [updater]
     */
    override fun postUpdateCallback(updatedTimetables: Collection<TimetableInfo>) {
        val updated = updatedTimetables.size

        if (updated == 0) {
            Log.i(logTag, "Post-Update callback received but no timetables were updated")
        } else {
            Log.i(logTag, "Post-Update callback received. Updater updated $updated timetables")
            val notification = createNotification(updated)
            showNotification(notification)
        }
        stopSelf()
    }

    /**
     * Create a notification that will be displayed to the user
     * @param updated: Number of timetables that got updated
     */
    private fun createNotification(updated: Int): Notification {
        return NotificationCompat.Builder(this, notificationChannelId)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentTitle("Timetable Update")
            .setContentText("Updated $updated timetable(s)")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH).build()
    }

    /**
     * Show the notification to the user
     */
    private fun showNotification(notification: Notification) {
        val notificationId = 1
        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, notification)
        }
    }

    override fun onBind(intent: Intent): IBinder? = null
}
