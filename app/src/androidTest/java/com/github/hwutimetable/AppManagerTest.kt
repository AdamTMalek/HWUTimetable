package com.github.hwutimetable

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.github.hwutimetable.extensions.getSharedPreferences
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test


@HiltAndroidTest
class AppManagerTest {
    private val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val sharedPrefs = targetContext.getSharedPreferences(R.string.shared_pref_file_key, Context.MODE_PRIVATE)

    @Test
    fun testFirstRunIsTrueWhenNoPreferenceSet() {
        with(sharedPrefs.edit()) {
            remove(targetContext.getString(R.string.first_run))
            commit()
        }
        val appManager = AppManager(targetContext)
        assertTrue(appManager.isFirstRun)
    }

    @Test
    fun testFirstRunIsFalseWhenPreferenceSet() {
        with(sharedPrefs.edit()) {
            putBoolean(targetContext.getString(R.string.first_run), false)
            commit()
        }
        val appManager = AppManager(targetContext)
        assertFalse(appManager.isFirstRun)
    }
}