package com.example.hwutimetable.updater

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.IBinder
import android.util.Log
import androidx.preference.PreferenceManager
import com.example.hwutimetable.filehandler.TimetableInfo
import com.example.hwutimetable.parser.Parser
import com.example.hwutimetable.scraper.Scraper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * [UpdateService] is a service that is responsible for starting the update process the timetables stored on the device
 * (using [Updater]), even if the app is not currently open. The service will register another notification
 * receiver (i.e. [UpdateNotificationReceiver]) if it is specified in the intent extras. This extra receiver
 * may be used to create Android notifications informing the user about ongoing update or the result of it.
 */
class UpdateService : Service(), UpdateNotificationReceiver {
    private val logTag = "UpdateService"
    private lateinit var updater: UpdatePerformer

    override fun onCreate() {
    }

    /**
     * Register this object as an [UpdateNotificationReceiver] to the [updater]
     */
    private fun registerSelfAsReceiver() {
        updater.addNotificationReceiver(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        GlobalScope.launch {
            configurePerformer(intent)
            configureNotifier(intent)
            registerSelfAsReceiver()

            if (canUpdate()) {
                Log.i(logTag, "Update alarm has been triggered, starting the update process")
                updater.update()
            } else {
                Log.i(logTag, "Update alarm has been triggered but was no allowed transport method was available.")
            }
        }
        return START_STICKY
    }

    private fun configurePerformer(intent: Intent?) {
        updater = getDefaultUpdater()
        if (intent == null || intent.extras == null)
            return

        updater = intent.extras!!.get("performer") as UpdatePerformer? ?: getDefaultUpdater()
    }

    private fun configureNotifier(intent: Intent?) {
        if (intent == null || intent.extras == null) {
            updater.addNotificationReceiver(getDefaultNotifier())
            return
        }

        val notifier = intent.extras!!.get("notifier") as UpdateNotificationReceiver? ?: getDefaultNotifier()
        updater.addNotificationReceiver(notifier)
    }

    private fun getDefaultUpdater() = Updater(this.filesDir, Parser(), Scraper())

    private fun getDefaultNotifier() = UpdateNotifier(this)

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

    private fun canUseMobileData(sharedPreferences: SharedPreferences): Boolean {
        return sharedPreferences.getBoolean("allow_data", false)
    }

    private fun isWifiEnabled(networkCapabilities: NetworkCapabilities): Boolean {
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun isMobileDataEnabled(networkCapabilities: NetworkCapabilities): Boolean {
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    /**
     * Post-update callback received from the [updater]
     */
    override fun onUpdateFinished(updated: Collection<TimetableInfo>) {
        val logMessage = if (updated.isEmpty())
            "Post-Update callback received but no timetables were updated"
        else
            "Post-Update callback received. Updater updated $updated timetables"

        Log.i(logTag, logMessage)
        stopSelf()
    }

    override fun onUpdateFinished() {
        return
    }

    override fun onUpdateInProgress() {
        return
    }

    override fun onBind(intent: Intent): IBinder? = null
}