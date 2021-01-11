package com.github.hwutimetable

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import java.util.*


/**
 * The [NoOptimizationRequest] class is responsible for checking and requesting
 * no battery optimizations for known "bad" manufacturers whose optimizations
 * break the functionality of the app (update checks).
 *
 * Additionally, for Xiaomi, autostart permission will be requested as it is known
 * to make a difference.
 */
class NoOptimizationRequest(private val context: Context) {
    private val logTag = "BatteryOptimization"

    /**
     * Returns true if no optimization request should be made.
     * False if they are already off, or manufacturer is not on the "bad list".
     */
    fun shouldRequestNoOptimization(): Boolean {
        val powerManager = context.getSystemService(PowerManager::class.java)
        return if (powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
            Log.d(logTag, "Optimization already off.")
            false
        } else {
            Log.d(logTag, "Requesting no optimization")
            isManufacturerBad()
        }
    }

    /**
     * Checks if the device manufacturer is on the "bad list".
     *
     * List containing manufacturers that have battery optimizations
     * that break the functionality of Android, and as a result
     * background workers will be cancelled as soon as the app is killed.
     */
    private fun isManufacturerBad() = android.os.Build.MANUFACTURER.toLowerCase(Locale.getDefault()) in listOf(
        "xiaomi",
        "oneplus",
        "huawei",
    )

    /**
     * Request no battery optimizations, and for Xiaomi additionally request the autostart permission
     */
    fun requestNoOptimizations() {
        AlertDialog.Builder(context).run {
            val message = "${android.os.Build.MANUFACTURER} ${context.getString(R.string.battery_opt_dialog_text)}"
            setMessage(message)
            setPositiveButton(R.string.battery_opt_continue) { _, _ ->
                takeUserToBatteryOptimizationSettings()
            }
            create()
        }.show()

        if (android.os.Build.MANUFACTURER.equals("xiaomi", ignoreCase = true)) {
            requestAutostart()
        }
    }

    /**
     * Request Autostart for Xiaomi devices.
     */
    private fun requestAutostart() {
        Log.d(logTag, "Requesting autostart permission")
        AlertDialog.Builder(context).run {
            val message = "${android.os.Build.MANUFACTURER} ${context.getString(R.string.autostart_dialog_text)}"
            setMessage(message)
            setPositiveButton(R.string.battery_opt_continue) { _, _ ->
                takeUserToAutostartSettings()
            }
        }.show()
    }

    private fun takeUserToBatteryOptimizationSettings() {
        val intent = Intent().apply {
            action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            data = Uri.parse("package:" + context.packageName)
        }
        context.startActivity(intent)
    }

    private fun takeUserToAutostartSettings() {
        val intent = Intent().apply {
            component = ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )
        }
        context.startActivity(intent)
    }
}