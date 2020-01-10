package com.example.hwutimetable.settings

import android.os.Bundle
import android.text.format.DateFormat
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.hwutimetable.R
import com.example.hwutimetable.updater.UpdateManager
import org.joda.time.format.DateTimeFormat


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.settings,
                SettingsFragment()
            )
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed(); true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private lateinit var updateManager: UpdateManager

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            updateManager = UpdateManager(context!!)
            PreferenceManager.getDefaultSharedPreferences(context!!)
                .registerOnSharedPreferenceChangeListener(updateManager)

            setTimePreferenceSummaryProvider()
            setIntervalPreferenceSummaryProvider()
        }

        override fun onDisplayPreferenceDialog(preference: Preference?) {
            when (preference) {
                is TimePreference -> displayTimePreference(preference)
                is NumberPreference -> displayNumberPreference(preference)
                else -> super.onDisplayPreferenceDialog(preference)
            }
        }

        /**
         * Set the summary provider of the update time preference control
         */
        private fun setTimePreferenceSummaryProvider() {
            val timePreference: TimePreference? = findPreference("time_preference")

            timePreference?.summaryProvider = Preference.SummaryProvider<TimePreference> { preference ->
                val stringFormat = if (DateFormat.is24HourFormat(context)) {
                    "HH:mm"
                } else {
                    "h:mm a"
                }

                val formatter = DateTimeFormat.forPattern(stringFormat)
                val timeAsString = preference.time!!.toString(formatter)
                "The update time is currently set to: $timeAsString. Click here to change it."
            }
        }

        /**
         * Set the summary provider of the interval (frequency) preference control
         */
        private fun setIntervalPreferenceSummaryProvider() {
            val intervalPreference: NumberPreference? = findPreference("frequency_preference")

            intervalPreference?.summaryProvider = Preference.SummaryProvider<NumberPreference> { preference ->
                "Update checks will be performed every".plus(
                    when (preference.value) {
                        1 -> "day"
                        else -> " ${preference.value} days"
                    }
                ).plus(". Click here to change it.")
            }
        }

        private fun displayTimePreference(preference: Preference) {
            TimePreferenceDialogFragmentCompat.newInstance(preference.key).let {
                it.setTargetFragment(this, 0)
                it.show(fragmentManager!!, "androidx.support.preference.PreferenceFragment.DIALOG")
            }
        }

        private fun displayNumberPreference(preference: Preference) {
            NumberPreferenceDialogFragmentCompat.newInstance(preference.key).let {
                it.setTargetFragment(this, 0)
                it.show(fragmentManager!!, "androidx.support.preference.PreferenceFragment.DIALOG")
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}