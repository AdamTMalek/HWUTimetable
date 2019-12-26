package com.example.hwutimetable.settings

import android.content.Context
import android.content.res.TypedArray
import android.text.format.DateFormat
import android.util.AttributeSet
import androidx.preference.DialogPreference
import androidx.preference.PreferenceManager
import com.example.hwutimetable.R
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat

class TimePreference(context: Context?, attrs: AttributeSet?) : DialogPreference(context, attrs) {
    var time: LocalTime? = null
        set(value) {
            field = value
            if (value != null) {
                val minutesAfterMidnight = (value.hourOfDay * 60) + value.minuteOfHour
                persistInt(minutesAfterMidnight)
            }
            setSummary()
        }

    private val dialogLayoutResId = R.layout.time_pref_dialog

    init {
        setPositiveButtonText(android.R.string.ok)
        setNegativeButtonText(android.R.string.cancel)
        setTimeOnInit()
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInt(index, 0)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        when (defaultValue) {
            is LocalTime -> time = defaultValue
            is Int -> time = LocalTime(defaultValue / 60, defaultValue % 60)
        }
    }

    override fun getDialogLayoutResource() = dialogLayoutResId


    private fun setTimeOnInit() {
        val default = LocalTime.now().let { (it.hourOfDay * 60) + it.minuteOfHour }
        val persisted = PreferenceManager.getDefaultSharedPreferences(context).getInt(key, default)
        time = LocalTime(persisted / 60, persisted % 60)
    }

    private fun setSummary() {
        if (time == null) {
            this.summary = "Update time not set"
            return
        }

        val stringFormat = if (DateFormat.is24HourFormat(context)) {
            "HH:mm"
        } else {
            "h:mm a"
        }

        val formatter = DateTimeFormat.forPattern(stringFormat)
        val timeAsString = time!!.toString(formatter)
        this.summary = "The update time is currently set to: $timeAsString. Click here to change it."
    }
}