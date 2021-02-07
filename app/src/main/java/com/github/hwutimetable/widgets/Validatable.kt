package com.github.hwutimetable.widgets

import com.github.hwutimetable.validators.Validator

/**
 * [Validatable] is an interface that any validatable widget has to implement.
 * The [Validatable] interface accepts validators for the specific widget type.
 */
interface Validatable<T : Validator<*>> {
    /**
     * True when all validations pass.
     */
    val isValid: Boolean

    /**
     * Adds validator to the list of validators
     */
    fun addValidator(vararg validator: T)

    /**
     * Specifies the function that will be executed on failed validation.
     */
    fun onValidationFailure(onInvalid: () -> Unit)

    /**
     * Specifies the function that will be executed on successful validation.
     */
    fun onValidationSuccess(onValid: () -> Unit)
}
