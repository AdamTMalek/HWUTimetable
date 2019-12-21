package com.example.hwutimetable

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference

class TimePreference(context: Context?, attrs: AttributeSet?) : DialogPreference(context, attrs) {
    var time: Int = 0
        set(value) {
            field = value
            persistInt(value)
        }

    private val dialogLayoutResId = R.layout.time_pref_dialog

    init {
        setPositiveButtonText(android.R.string.ok)
        setNegativeButtonText(android.R.string.cancel)
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInt(index, 0)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        if (defaultValue is Int) {
            time = defaultValue
        }
    }

    override fun getDialogLayoutResource() = dialogLayoutResId
}