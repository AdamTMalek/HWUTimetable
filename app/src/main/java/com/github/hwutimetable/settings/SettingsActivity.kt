package com.github.hwutimetable.settings

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.github.hwutimetable.R
import com.github.hwutimetable.network.NetworkUtilities
import com.github.hwutimetable.parser.Parser
import com.github.hwutimetable.parser.Timetable
import com.github.hwutimetable.scraper.Scraper
import com.github.hwutimetable.updater.OnUpdateFinishedListener
import com.github.hwutimetable.updater.UpdateManager
import com.github.hwutimetable.updater.UpdateNotifier
import com.github.hwutimetable.updater.Updater
import org.joda.time.format.DateTimeFormat
import java.util.*


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

    class SettingsFragment : PreferenceFragmentCompat(), OnUpdateFinishedListener,
        NetworkUtilities.ConnectivityCallbackReceiver {
        private lateinit var updateManager: UpdateManager
        private val networkUtilities: NetworkUtilities by lazy {
            NetworkUtilities(this.context!!)
        }
        private val updateNowPreference: Preference by lazy {
            findPreference<Preference>(getString(R.string.update_now))!!
        }

        private val connectivityCallback: NetworkUtilities.ConnectivityCallback by lazy {
            NetworkUtilities.ConnectivityCallback(context!!)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            updateManager = UpdateManager(context!!)
            PreferenceManager.getDefaultSharedPreferences(context!!)
                .registerOnSharedPreferenceChangeListener(updateManager)

            setTimePreferenceSummaryProvider()
            setIntervalPreferenceSummaryProvider()
            setUpdateButtonHandler()
            setUpdateSummary()

            connectivityCallback.registerCallbackReceiver(this)

            if (!networkUtilities.hasInternetConnection())
                onConnectionLost()
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

        private fun setUpdateButtonHandler() {
            val button = findPreference<Preference>(getString(R.string.update_now))
            button!!.setOnPreferenceClickListener {
                startUpdate()
                return@setOnPreferenceClickListener true
            }
        }

        private fun setUpdateSummary() {
            val preference = findPreference<SwitchPreferenceCompat>("enable_updates")!!
            val preferencesName = context!!.getString(R.string.update_details)
            val sharedPreferences = activity!!.getSharedPreferences(preferencesName, Context.MODE_PRIVATE) ?: return
            val lastUpdateTimestamp = sharedPreferences.getInt(getString(R.string.last_update), 0)

            val summary = if (lastUpdateTimestamp != 0) {
                val date = Date(lastUpdateTimestamp.toLong() * 1000)
                val dateFormat = DateFormat.getDateFormat(context!!)
                val timeFormat = DateFormat.getTimeFormat(context!!)
                "Last checked on ${dateFormat.format(date)} at ${timeFormat.format(date)}"
            } else {
                "No update checks have been performed yet"
            }

            preference.summary = summary
        }

        /**
         * Starts the update-check process using [Scraper] and [Parser], as well as [UpdateNotifier]
         * to show "update in progress" notification.
         */
        private fun startUpdate() {
            val activity = this.activity!!
            val context = activity.applicationContext
            val updater = Updater(activity.filesDir, Parser(null), Scraper(), activity)
            val notifier = UpdateNotifier(context)

            updater.addInProgressListener(notifier)
            updater.addFinishedListener(notifier)
            updater.addFinishedListener(this)
            updater.update()
        }

        override fun onConnectionAvailable() {
            activity!!.runOnUiThread {
                updateNowPreference.isEnabled = true
                updateNowPreference.summary = getString(R.string.update_now_enabled_summary)
            }
        }

        override fun onConnectionLost() {
            activity!!.runOnUiThread {
                updateNowPreference.isEnabled = false
                updateNowPreference.summary = getString(R.string.update_now_disabled_summary)
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onUpdateFinished(updated: Collection<Timetable.TimetableInfo>) {
            val message = when (updated.isEmpty()) {
                true -> "All timetables are up-to-date"
                false -> "Updated ${updated.size} timetable(s)"
            }

            Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
            setUpdateSummary()
        }

        override fun onDestroy() {
            connectivityCallback.cleanup()
            super.onDestroy()
        }
    }
}