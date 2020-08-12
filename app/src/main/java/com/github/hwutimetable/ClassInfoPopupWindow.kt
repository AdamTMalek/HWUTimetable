package com.github.hwutimetable

import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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
            val screenWidth = getScreenWidth(context)
            val (width, height) = getWindowDimensions(context, screenWidth)
            val view = getView(context, item)
            return ClassInfoPopupWindow(view, width, height, true)
        }

        private fun getLayoutInflaterService(context: Context) =
            context.getSystemService(LayoutInflater::class.java)!!

        private fun getView(context: Context, item: TimetableItem) =
            getLayoutInflaterService(context).inflate(R.layout.timetable_class_info_view, null)
                .apply {
                    ClassInfoViewPopulator.populateView(this, item)
                }

        private fun getScreenWidth(context: Context): Int {
            val displayMetrics = DisplayMetrics()
            context.getSystemService(WindowManager::class.java)!!
                .defaultDisplay
                .getMetrics(displayMetrics)

            return displayMetrics.widthPixels
        }

        private fun getWindowDimensions(context: Context, width: Int): Pair<Int, Int> {
            return Pair(
                width - context.resources.getDimensionPixelSize(R.dimen.info_window_side_margin),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
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