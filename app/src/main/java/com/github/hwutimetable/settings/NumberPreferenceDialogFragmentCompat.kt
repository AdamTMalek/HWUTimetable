package com.github.hwutimetable.settings

import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.preference.PreferenceDialogFragmentCompat
import com.github.hwutimetable.R

class NumberPreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat() {
    private var numberPicker: NumberPicker? = null

    companion object {
        fun newInstance(key: String): NumberPreferenceDialogFragmentCompat {
            val bundle = Bundle(1).apply { putString(ARG_KEY, key) }
            return NumberPreferenceDialogFragmentCompat().apply { arguments = bundle }
        }
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)

        numberPicker = view.findViewById(R.id.num_picker) as NumberPicker

        if (preference is NumberPreference) {
            val numPreference = preference as NumberPreference
            val max = numPreference.maxNumber
            val min = numPreference.minNumber
            val value = numPreference.value
            val step = numPreference.step

            with(numberPicker!!) {
                this.maxValue = max
                this.minValue = min
                this.value = value
                this.displayedValues = (minValue..maxValue step step).map { it.toString() }.toTypedArray()
            }
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (!positiveResult)
            return

        val value = numberPicker!!.value

        if (preference is NumberPreference) {
            if (preference.callChangeListener(value)) {
                (preference as NumberPreference).value = value
            }
        }
    }
}