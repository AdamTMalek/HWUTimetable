package com.github.hwutimetable.widgets

import android.content.Context
import android.util.AttributeSet
import com.github.hwutimetable.validators.EditTextValidator


/**
 * [ValidatableEditText] is a [Validatable] widget that
 * can be validated with a [EditTextValidator].
 */
class ValidatableEditText(context: Context, attrs: AttributeSet?) :
    androidx.appcompat.widget.AppCompatEditText(context, attrs), Validatable<EditTextValidator> {

    private val validators = mutableSetOf<EditTextValidator>()
    private var onValid: () -> Unit = { }
    private var onInvalid: () -> Unit = { }

    override val isValid
        get() = validators.all { it.validate(this) }

    constructor(context: Context) : this(context, null)

    init {
        // Because the handler methods (onValid and onInvalid) will be changed,
        // wrapper functions will be passed to the constructor of the TextWatcherValidator.
        val textWatcher = TextWatcherValidator(validators, this, { onValid() }, { onInvalid })
        this.addTextChangedListener(textWatcher)
    }

    override fun addValidator(vararg validator: EditTextValidator) {
        validators.addAll(validator)
    }

    override fun onValidationFailure(onInvalid: () -> Unit) {
        this.onInvalid = onInvalid
    }

    override fun onValidationSuccess(onValid: () -> Unit) {
        this.onValid = onValid
    }
}