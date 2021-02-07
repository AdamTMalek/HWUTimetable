package com.github.hwutimetable.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import com.github.hwutimetable.validators.EditTextValidator
import com.github.hwutimetable.validators.Validator


/**
 * [ValidatableAutoCompleteTextView] is a [Validatable] widget that
 * can be validated with a [EditTextValidator].
 */
class ValidatableAutoCompleteTextView(context: Context, attrs: AttributeSet?) :
    androidx.appcompat.widget.AppCompatAutoCompleteTextView(context, attrs), Validatable<Validator<EditText>> {
    private val validators = mutableSetOf<com.github.hwutimetable.validators.Validator<EditText>>()
    private var onValid: () -> Unit = { }
    private var onInvalid: () -> Unit = { }

    override val isValid: Boolean
        get() = validators.all { it.validate(this) }

    init {
        // Because the handler methods (onValid and onInvalid) will be changed,
        // wrapper functions will be passed to the constructor of the TextWatcherValidator.
        this.addTextChangedListener(TextWatcherValidator(validators, this, { onValid() }, { onInvalid() }))
    }

    override fun addValidator(vararg validator: com.github.hwutimetable.validators.Validator<EditText>) {
        validators.addAll(validator)
    }

    override fun onValidationFailure(onInvalid: () -> Unit) {
        this.onInvalid = onInvalid
    }

    override fun onValidationSuccess(onValid: () -> Unit) {
        this.onValid = onValid
    }
}