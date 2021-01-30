package com.github.hwutimetable.settings

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.github.hwutimetable.R
import org.joda.time.format.DateTimeFormat
import java.util.*


/**
 * The [UpdatePreferenceFragment] is a [PreferenceFragmentCompat] that manages preferences
 * (i.e. sets summaries and handles dialogs for custom controls) for the preferences
 * defined in [R.xml.update_preferences].
 *
 * Note that these preferences do not include the force-updates button.
 */
class UpdatePreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpdateSummary()
        setTimePreferenceSummaryProvider()
        setIntervalPreferenceSummaryProvider()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.update_preferences, rootKey)
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        when (preference) {
            is TimePreference -> displayTimePreference(preference)
            is NumberPreference -> displayNumberPreference(preference)
            else -> super.onDisplayPreferenceDialog(preference)
        }
    }

    private fun displayTimePreference(preference: Preference) {
        TimePreferenceDialogFragmentCompat.newInstance(preference.key).let {
            it.setTargetFragment(this, 0)
            it.show(parentFragmentManager, "androidx.support.preference.PreferenceFragment.DIALOG")
        }
    }

    private fun displayNumberPreference(preference: Preference) {
        NumberPreferenceDialogFragmentCompat.newInstance(preference.key).let {
            it.setTargetFragment(this, 0)
            it.show(parentFragmentManager, "androidx.support.preference.PreferenceFragment.DIALOG")
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
}