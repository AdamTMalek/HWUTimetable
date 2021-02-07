package com.github.hwutimetable.validators

import android.widget.EditText


/**
 * [ConstrainedNameValidator] is a validator for any [EditText]-based view
 * that requires the text of the widget to be from the list given by [getNames].
 *
 * @param getNames Method that returns collection of acceptable names. As it is a method,
 *                 this allows the collection to be dynamic, as the validator will fetch
 *                 the names on every validation call.
 */
class ConstrainedNameValidator(private val getNames: () -> Collection<String>) : Validator<EditText> {
    /**
     * Due to the usage of the [ConstrainedNameValidator], we won't display any errors.
     */
    override val errorString: String?
        get() = null

    override fun validate(widget: EditText): Boolean {
        return widget.text.toString() in getNames()
    }
}
