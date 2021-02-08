package com.github.hwutimetable.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.github.hwutimetable.validators.Validator


/**
 * [ValidatableRecyclerView] is a [Validatable] widget that can be validated
 * through the use of [Validator] for [RecyclerView].
 * All validators will be run on data change of the adapter.
 */
class ValidatableRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs),
    Validatable<Validator<RecyclerView>> {
    private val validators = mutableListOf<Validator<RecyclerView>>()
    private var onInvalidFn: () -> Unit = { }
    private var onValidFn: () -> Unit = { }

    override val isValid: Boolean
        get() = validators.all { it.validate(this) }


    override fun setAdapter(adapter: Adapter<*>?) {
        // Override setAdapter, as we need to set the data observer
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                for (validator in this@ValidatableRecyclerView.validators) {
                    if (!validator.validate(this@ValidatableRecyclerView))
                        onInvalidFn()
                }

                onValidFn()
            }
        })
    }

    override fun addValidator(vararg validator: Validator<RecyclerView>) {
        validators.addAll(validator)
    }

    override fun onValidationFailure(onInvalid: () -> Unit) {
        onInvalidFn = onInvalid
    }

    override fun onValidationSuccess(onValid: () -> Unit) {
        onValidFn = onValid
    }
}