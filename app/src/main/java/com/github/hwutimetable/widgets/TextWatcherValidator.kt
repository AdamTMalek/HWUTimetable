package com.github.hwutimetable.widgets

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.github.hwutimetable.validators.Validator


/**
 * [TextWatcherValidator] is a class used by [Validatable] widgets
 * that are based on [EditText] and whose validation needs to be triggered
 * upon change of the text. It is implemented as a [TextWatcher].
 *
 * @param validators [Collection] of [Validator] for [EditText].
 * @param widget [EditText] to which this validator belongs to.
 * @param onValid Will be executed when all validations pass.
 * @param onInvalid Will be executed when a validation fails.
 */
class TextWatcherValidator(
    private val validators: Collection<Validator<EditText>>,
    private val widget: EditText,
    private val onValid: () -> Unit,
    private val onInvalid: () -> Unit
) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // Unused
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // Unused
    }

    override fun afterTextChanged(s: Editable?) {
        for (validator in validators) {
            if (!validator.validate(widget)) {
                widget.error = validator.errorString
                onInvalid()
                return
            }
        }

        onValid()
    }
}