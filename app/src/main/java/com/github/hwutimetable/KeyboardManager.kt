package com.github.hwutimetable

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager

object KeyboardManager {
    fun hideKeyboard(activity: Activity) {
        val inputManager = activity.getSystemService(InputMethodManager::class.java)!!
        val view = activity.currentFocus ?: View(activity)
        inputManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}