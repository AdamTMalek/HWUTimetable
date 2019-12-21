package com.example.hwutimetable

import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.TimePicker
import androidx.preference.PreferenceDialogFragmentCompat

class TimePreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat() {
    private var timePicker: TimePicker? = null

    companion object {
        fun newInstance(key: String): TimePreferenceDialogFragmentCompat {
            val bundle = Bundle(1).apply { putString(ARG_KEY, key) }
            return TimePreferenceDialogFragmentCompat().apply { arguments = bundle }
        }
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)

        timePicker = view.findViewById(R.id.time_picker) as TimePicker

        var minutesAfterMidnight: Int? = null
        if (preference is TimePreference) {
            minutesAfterMidnight = (preference as TimePreference).time
        }

        if (minutesAfterMidnight != null) {
            val hour = minutesAfterMidnight / 60
            val minute = minutesAfterMidnight % 60
            val is24Hour = DateFormat.is24HourFormat(context)

            with(timePicker!!) {
                setIs24HourView(is24Hour)
                this.hour = hour
                this.minute = minute
            }
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (!positiveResult)
            return

        val hour = timePicker!!.hour
        val minute = timePicker!!.minute
        val minutesAfterMidnight = (hour * 60) + minute

        if (preference is TimePreference) {
            if (preference.callChangeListener(minutesAfterMidnight)) {
                (preference as TimePreference).time = minutesAfterMidnight
            }
        }
    }

}