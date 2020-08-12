package com.github.hwutimetable

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.github.hwutimetable.parser.TimetableItem
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * The [ClassInfoPopupWindow] is a [PopupWindow] that displays
 * information about a timetable class. It is especially useful
 * when using simplified view, as details are not given
 *
 * The class does not have a public constructor.
 * Use [ClassInfoPopupWindow.create] to create an object of this class.
 */
class ClassInfoPopupWindow private constructor(contentView: View?, width: Int, height: Int, focusable: Boolean) :
    PopupWindow(contentView, width, height, focusable) {

    companion object {
        /**
         * Creates an object of [ClassInfoPopupWindow] for the given [item].
         */
        fun create(context: Context, item: TimetableItem): ClassInfoPopupWindow {
            val view = getView(context, item)
            val width = LinearLayout.LayoutParams.WRAP_CONTENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT
            val focusable = true

            return ClassInfoPopupWindow(view, width, height, focusable)
        }

        private fun getLayoutInflaterService(context: Context): LayoutInflater =
            context.getSystemService(LayoutInflater::class.java)!!

        private fun getView(context: Context, item: TimetableItem) =
            getLayoutInflaterService(context).inflate(R.layout.timetable_class_info_view, null)
                .apply {
                    ClassInfoViewPopulator.populateView(this, item)
                }
    }

    init {
        addCloseButtonClickHandler()
    }

    private fun addCloseButtonClickHandler() {
        this.contentView.findViewById<FloatingActionButton>(R.id.close_class_info).setOnClickListener {
            this.dismiss()
        }
    }
}