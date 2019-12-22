package com.example.hwutimetable.updater

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
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
    private lateinit var updater: UpdatePerformer

    override fun onCreate() {
        if (checkNotificationChannelExists())
            createNotificationChannel()

        updater = Updater(this.filesDir)
        registerSelfAsReceiver()
    }

    /**
     * Register this object as an [UpdateNotificationReceiver] to the [updater]
     */
    private fun registerSelfAsReceiver() {
        updater.addNotificationReceiver(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // TODO: Delete after tested
        val notification = createTestNotification()
        showNotification(notification, 2)

        if (canUpdate()) {
            Log.i(logTag, "Update alarm has been triggered, starting the update process")
            updater.update()
        } else {
            Log.i(logTag, "Update alarm has been triggered but was no allowed transport method was available.")
        }
        return START_STICKY
    }

    /**
     * This method will check if the update service can start the update process.
     * It will check if the currently enabled transport methods can be used
     * to perform the update process. This is to ensure that update will not be performed
     * via mobile data if the users do not want to use it for the process.
     */
    private fun canUpdate(): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        if (isWifiEnabled(capabilities) && canUseWifi(preferences))
            return true
        else if (isMobileDataEnabled(capabilities) && canUseMobileData(preferences))
            return true
        return false
    }

    private fun canUseWifi(sharedPreferences: SharedPreferences): Boolean {
        return sharedPreferences.getBoolean("allow_wifi", true)
    }

    private fun isWifiEnabled(networkCapabilities: NetworkCapabilities): Boolean {
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun canUseMobileData(sharedPreferences: SharedPreferences): Boolean {
        return sharedPreferences.getBoolean("allow_data", false)
    }

    private fun isMobileDataEnabled(networkCapabilities: NetworkCapabilities): Boolean {
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
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
            showNotification(notification, 1)
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

    // TODO: Delete after tested
    private fun createTestNotification(): Notification {
        return NotificationCompat.Builder(this, notificationChannelId)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentTitle("Timetable Update")
            .setContentText("Update in progress")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MAX).build()
    }

    /**
     * Show the notification to the user
     */
    private fun showNotification(notification: Notification, notificationId: Int) {
        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, notification)
        }
    }

    override fun onBind(intent: Intent): IBinder? = null
}
