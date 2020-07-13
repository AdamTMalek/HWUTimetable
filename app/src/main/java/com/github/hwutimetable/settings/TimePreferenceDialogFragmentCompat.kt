package com.github.hwutimetable.settings

import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.TimePicker
import androidx.preference.PreferenceDialogFragmentCompat
import com.github.hwutimetable.R
import org.joda.time.LocalTime

class TimePreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat() {
    private var timePicker: TimePicker? = null

    companion object {
        fun newInstance(key: String): TimePreferenceDialogFragmentCompat {
            val bundle = Bundle(1).apply { putString(ARG_KEY, key) }
            return TimePreferenceDialogFragmentCompat()
                .apply { arguments = bundle }
        }
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)

        timePicker = view.findViewById(R.id.time_picker) as TimePicker

        if (preference is TimePreference) {
            val time = (preference as TimePreference).time
            val is24Hour = DateFormat.is24HourFormat(context)

            if (time == null)
                return

            with(timePicker!!) {
                setIs24HourView(is24Hour)
                this.hour = time.hourOfDay
                this.minute = time.minuteOfHour
            }
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (!positiveResult)
            return

        val time = LocalTime(timePicker!!.hour, timePicker!!.minute)

        if (preference is TimePreference) {
            if (preference.callChangeListener(time)) {
                (preference as TimePreference).time = time
            }
        }
    }

}