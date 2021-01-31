package com.github.hwutimetable

import android.content.Context


/**
 * The [AppManager] class contains methods for determining
 * if the [AppManager] process (or any process that should only
 * run when the application is freshly installed) should be run.
 */
class AppManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences(
        context.getString(R.string.shared_pref_file_key),
        Context.MODE_PRIVATE
    )

    private val firstRunPrefKey = context.getString(R.string.first_run)

    /**
     * True when it is the first time application is running
     */
    val isFirstRun = sharedPreferences.getBoolean(firstRunPrefKey, true)

    /**
     * Sets the first run flag (in shared preferences) to false.
     */
    fun setFirstRunToFalse() {
        with(sharedPreferences.edit()) {
            putBoolean(firstRunPrefKey, false)
            commit()
        }
    }
}