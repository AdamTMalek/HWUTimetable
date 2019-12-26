package com.example.hwutimetable.settings

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference
import androidx.preference.PreferenceManager
import com.example.hwutimetable.R
import org.joda.time.LocalTime


/**
 * TimePreference is a DialogPreference that can be used for
 * storing local time preference of the user.
 */
class TimePreference(context: Context?, attrs: AttributeSet?) : DialogPreference(context, attrs) {
    var time: LocalTime? = null
        set(value) {
            field = value
            if (value != null) {
                val minutesAfterMidnight = (value.hourOfDay * 60) + value.minuteOfHour
                persistInt(minutesAfterMidnight)
            }
            notifyChanged()
        }

    private val dialogLayoutResId = R.layout.time_pref_dialog

    init {
        setPositiveButtonText(android.R.string.ok)
        setNegativeButtonText(android.R.string.cancel)
        setTimeOnInit()
    }

    /**
     * Set the time to the persisted value if it exists, otherwise set it to the default value
     */
    private fun setTimeOnInit() {
        val default = LocalTime.now().let { (it.hourOfDay * 60) + it.minuteOfHour }
        val persisted = PreferenceManager.getDefaultSharedPreferences(context).getInt(key, default)
        time = LocalTime(persisted / 60, persisted % 60)
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
}