package com.github.hwutimetable.validators

import android.view.ViewGroup
import androidx.core.view.allViews
import com.github.hwutimetable.widgets.Validatable


/**
 * The [FormValidator] is a class that handles a form (that is the passed [ViewGroup]).
 *
 * The validator will scan the [ViewGroup] for any view that implements the [Validatable]
 * interface. Such a view will be then monitored by the form validator, and if all fields
 * are valid the [onValid] method will be called. This also includes the moment when the
 * validator is created.
 *
 * Additionally, upon change to any of the [Validatable] views, the [FormValidator] will
 * be notified if the validation of a given view fails, then the [onInvalid] method will
 * be called.
 *
 * @param rootView: View Group containing [Validatable] fields. Note that other, non-[Validatable] fields are allowed.
 * @param onValid: Called when all [Validatable] views in the given [ViewGroup] are valid.
 * @param onInvalid: Called when a [Validatable] view has failed validation in the given [ViewGroup]
 */
class FormValidator(rootView: ViewGroup, private val onValid: () -> Unit, private val onInvalid: () -> Unit) {
    private val invalidViews = mutableSetOf<Validatable<*>>()

    init {
        setValidator(rootView)
    }

    /**
     * Scans through all views of [rootView] and if it is a [Validatable] view,
     * sets its handling functions, and if it is invalid, it will be added to the
     * [invalidViews] set.
     */
    private fun setValidator(rootView: ViewGroup) {
        rootView.allViews.forEach { view ->
            if (view is Validatable<*>) {
                if (!view.isValid)
                    addInvalidView(view)

                view.onValidationSuccess {
                    removeInvalidView(view)
                }
                view.onValidationFailure {
                    addInvalidView(view)
                }
            }
        }
    }

    /**
     * This function will add the given [view] into the [invalidViews] list
     * and, if it is the first invalid view, the [onInvalid] method will be called.
     */
    private fun addInvalidView(view: Validatable<*>) {
        if (!invalidViews.add(view))
            return

        if (invalidViews.size == 1)
            onInvalid()
    }

    /**
     * This function will remove the given [view] from the [invalidViews] list
     * and, if it was the last invalid view, the [onValid] method will be called.
     */
    private fun removeInvalidView(view: Validatable<*>) {
        if (!invalidViews.remove(view))
            return

        if (invalidViews.isEmpty())
            onValid()
    }
}