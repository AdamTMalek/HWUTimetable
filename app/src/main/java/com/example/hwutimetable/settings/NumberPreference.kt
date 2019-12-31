package com.example.hwutimetable.settings

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference
import androidx.preference.PreferenceManager
import com.example.hwutimetable.R


/**
 * NumberPreference is a DialogPreference that can be used for
 * storing an integer preference of the user.
 * Min and max numbers can be changed by xml attributes.
 */
class NumberPreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {
    val maxNumber: Int
    val minNumber: Int

    var value: Int = 0 // This value will be changed by the init block
        set(value) {
            field = value
            persistInt(value)
            notifyChanged()
        }

    private val dialogLayoutResId = R.layout.num_pref_dialog

    init {
        // Get the min and max attributes from the xml
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.NumberPreference)
        maxNumber = attributes.getInt(R.styleable.NumberPreference_maxValue, 100)
        minNumber = attributes.getInt(R.styleable.NumberPreference_minValue, 0)
        this.value = PreferenceManager.getDefaultSharedPreferences(context).getInt(key, minNumber)
        attributes.recycle()

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
}

