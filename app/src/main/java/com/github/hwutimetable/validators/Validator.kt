package com.github.hwutimetable.validators

import android.view.View


/**
 * The [Validator] interface is an interface that validates [T]
 */
interface Validator<T : View> {
    /**
     * Performs validations on the widget [T]. Returns boolean when it is valid.
     */
    fun validate(widget: T): Boolean

    /**
     * Returns a string that will be used to display an error on the [T].
     */
    fun getInvalidInputError(): String
}
