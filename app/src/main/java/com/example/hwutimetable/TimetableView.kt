package com.example.hwutimetable

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView

object TimetableView {
    internal class TextViewProperties(
        val text: String,
        val columnWeight: Float,
        val columnSpan: Int?,
        val gravity: Int?
    )

    fun getTimetableItemView(context: Context, timetable: List<TimetableInfo>) {
        TODO("Construct the view")
    }

    private fun createMainGridLayout(context: Context): GridLayout {
        return GridLayout(context).also {
            it.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            it.columnCount = 2
            it.orientation = GridLayout.HORIZONTAL
        }
    }

    private fun createTimeTextView(context: Context, text: String): TextView {
        return TextView(context).also {
            it.text = text
            it.layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, 0.2f),
                GridLayout.spec(getTimeHeight(context), 1)
            )
        }
    }

    private fun createItemLinearLayout(span: Int, context: Context): LinearLayout {
        return LinearLayout(context).also {
            it.layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(0, 0.8f),
                GridLayout.spec(0, 1)
            ).apply {
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, span)
            }

            it.gravity = Gravity.FILL_VERTICAL
        }
    }

    private fun createItemGridLayout(context: Context): GridLayout {
        return GridLayout(context).also {
            it.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
            }

            it.orientation = GridLayout.HORIZONTAL
            it.columnCount = 3
            it.rowCount = 3
        }
    }

    private fun createItemTextView(context: Context, properties: TextViewProperties): TextView {
        val textView = TextView(context)
        textView.text = properties.text

        val layoutParams = GridLayout.LayoutParams(
            GridLayout.spec(GridLayout.UNDEFINED, properties.columnWeight),
            GridLayout.spec(GridLayout.UNDEFINED, 0)
        )

        if (properties.columnSpan != null) {
            layoutParams.rowSpec = GridLayout.spec(properties.columnSpan)
            textView.layoutParams = layoutParams
        }

        if (properties.gravity != null) {
            textView.gravity = properties.gravity
        }

        return textView
    }

    private fun getTimeHeight(context: Context): Int {
        val dp = 40
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}