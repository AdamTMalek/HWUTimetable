package com.example.hwutimetable.settings

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.example.hwutimetable.R

class NumberPreference(context: Context?, attrs: AttributeSet?) : DialogPreference(context, attrs) {
    val maxNumber = 14
    val minNumber = 1
    var value = minNumber
        set(value) {
            field = value
            setSummary()
            persistInt(value)
        }
    private val dialogLayoutResId = R.layout.num_pref_dialog

    init {
        setPositiveButtonText(android.R.string.ok)
        setNegativeButtonText(android.R.string.cancel)
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInt(index, minNumber)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        if (defaultValue is Int) {
            value = defaultValue
        }
    }

    override fun getDialogLayoutResource() = dialogLayoutResId

    private fun setSummary() {
        val summary = "Update checks will be performed every".plus(
            when (value) {
                1 -> "day"
                else -> " $value days"
            }
        ).plus(". Click here to change it.")
        this.summary = summary
    }
}

