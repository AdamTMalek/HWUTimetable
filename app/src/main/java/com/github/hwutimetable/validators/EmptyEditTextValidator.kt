package com.github.hwutimetable.validators

import android.widget.EditText


/**
 * [EmptyEditTextValidator] is an [EditTextValidator] that check
 * ensures the text in the widget that is validated is not empty.
 */
class EmptyEditTextValidator : EditTextValidator {
    override val errorString: String
        get() = "This field cannot be empty."

    override fun validate(widget: EditText): Boolean {
        return widget.text.isNotEmpty()
    }
}