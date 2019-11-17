package com.example.hwutimetable

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.hwutimetable.parser.TimetableDay

object TimetableView {
    internal class TextViewProperties(
        val text: String,
        val columnWeight: Float,
        val columnSpan: Int?,
        val gravity: Int?
    )

    fun getTimetableItemView(context: Context, timetable: TimetableDay): ScrollView {
        val scrollView = createScrollView(context)
        val gridLayout = createMainGridLayout(context)
        scrollView.addView(gridLayout)
        createHourLabels(context, gridLayout)
        return scrollView
    }

    private fun createScrollView(context: Context): ScrollView {
        return ScrollView(context).also {
            it.id = View.generateViewId()
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    private fun createHourLabels(context: Context, gridLayout: GridLayout) {
        for (i in 0..36) {
            val text = getHourLabelText(i)
            gridLayout.addView(
                createTimeTextView(context, text),
                getLayoutParams(i)
            )
        }
    }

    private fun getHourLabelText(index: Int): String {
        var hours = 9
        var minutes = 15

        for (i in 1..index) {
            minutes += 15
            if (minutes >= 60) {
                minutes = 0
                hours++
            }
        }
        return "$hours:${minutes.toString().padStart(2, '0')}"
    }

    private fun createMainGridLayout(context: Context): GridLayout {
        return GridLayout(context).also {
            it.id = View.generateViewId()
            it.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            it.columnCount = 2
            it.orientation = GridLayout.HORIZONTAL
        }
    }

    private fun createTimeTextView(context: Context, text: String): TextView {
        return TextView(context).also { it ->
            it.id = View.generateViewId()
            it.text = text
            it.width = 0
            it.height = getTimeHeight(context)
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

    private fun getLayoutParams(row: Int) = GridLayout.LayoutParams(
        GridLayout.spec(row),
        GridLayout.spec(0, 0.2f)
    )

    private fun getTimeHeight(context: Context): Int {
        val dp = 40
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}