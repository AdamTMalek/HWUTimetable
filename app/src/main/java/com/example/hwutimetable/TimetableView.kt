package com.example.hwutimetable

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.example.hwutimetable.parser.TimetableDay
import com.example.hwutimetable.parser.TimetableItem
import org.joda.time.LocalTime
import org.joda.time.Period

object TimetableView {

    fun getTimetableItemView(context: Context, timetable: TimetableDay): ScrollView {
        val scrollView = createScrollView(context)
        val gridLayout = createMainGridLayout(context)
        createHourLabels(context, gridLayout)
        createItems(context, gridLayout, timetable)
        scrollView.addView(gridLayout)
        return scrollView
    }

    private fun createHourLabels(context: Context, gridLayout: GridLayout) {
        for (i in 0..36) {
            val text = getHourLabelText(i)
            gridLayout.addView(
                createTimeTextView(context, text),
                getLayoutParams(i, 0, columnWeight = 0.2f)
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

    private fun createItems(context: Context, gridLayout: GridLayout, timetable: TimetableDay) {
        for (item in timetable.items) {
            val row = getRowIndexByTime(item.start)
            val rowspan = getRowspanByPeriod(item.duration)
            gridLayout.addView(
                createItem(context, item),
                getLayoutParams(row, 1, columnWeight = 0.8f, rowSpan = rowspan)
            )
        }
    }

    private fun getRowIndexByTime(localTime: LocalTime): Int {
        val dayStart = Period(9, 15, 0, 0)
        val itemStart = Period(localTime.hourOfDay, localTime.minuteOfHour, 0, 0)
        val differenceMinutes = itemStart.minus(dayStart).toStandardMinutes().minutes
        return differenceMinutes / 15;
    }

    private fun getRowspanByPeriod(period: Period): Int {
        return period.minutes / 15
    }

    private fun createItem(context: Context, item: TimetableItem): LinearLayout {
        val linearLayout = createItemLinearLayout(context, item.type.getColor(context))
        val gridLayout = createItemGridLayout(context)

        val code = createItemTextView(context, item.code, Gravity.LEFT)
        val weeks = createItemTextView(context, item.weeks, Gravity.CENTER_HORIZONTAL)
        val room = createItemTextView(context, item.room, Gravity.RIGHT)
        val name = createItemTextView(context, item.name, Gravity.CENTER_HORIZONTAL)
        val lecturer = createItemTextView(context, item.lecturer, Gravity.LEFT)
        val type = createItemTextView(context, item.type.name, Gravity.RIGHT)

        with (gridLayout) {
            addView(code, getLayoutParams(0, 0, columnWeight = 0.2f))
            addView(weeks, getLayoutParams(0, 1, columnWeight = 0.6f))
            addView(room, getLayoutParams(0, 2, columnWeight = 0.2f))
            addView(name, getLayoutParams(1, 0, columnSpan = 3))
            addView(lecturer, getLayoutParams(2, 0, columnWeight = 0.5f))
            addView(type, getLayoutParams(2, 2, columnSpan = 1, columnWeight = 0.5f))

        }

        linearLayout.addView(gridLayout)
        return linearLayout
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

    private fun createMainGridLayout(context: Context): GridLayout {
        return GridLayout(context).also {
            it.id = View.generateViewId()
            it.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            it.columnCount = 2
            it.orientation = GridLayout.HORIZONTAL
        }
    }

    private fun createTimeTextView(context: Context, text: String): TextView {
        return TextView(context).also {
            it.id = View.generateViewId()
            it.text = text
            it.width = 0
            it.height = getTimeHeight(context)
        }
    }

    private fun createItemLinearLayout(context: Context, color: Int): LinearLayout {
        return LinearLayout(context).also {
            it.id = View.generateViewId()
            it.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            it.gravity = Gravity.CENTER_VERTICAL
            it.background = ColorDrawable(color)
        }
    }

    private fun createItemGridLayout(context: Context): GridLayout {
        return GridLayout(context).also {
            it.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                getTimeHeight(context) * 4  // spreads the information across one full hour
            )

            it.orientation = GridLayout.HORIZONTAL
            it.columnCount = 3
            it.rowCount = 3
        }
    }

    private fun createItemTextView(context: Context, text: String, gravity: Int): TextView {
        return TextView(context).apply {
            this.text = text
            this.height = getTimeHeight(context)
            this.width = 0
            this.gravity = gravity
        }
    }

    private fun getLayoutParams(row: Int, column: Int,
                                rowWeight: Float = 1f,
                                columnWeight: Float = 1f,
                                rowSpan: Int = 1,
                                columnSpan: Int = 1) = GridLayout.LayoutParams(
        GridLayout.spec(row, rowSpan, rowWeight),
        GridLayout.spec(column, columnSpan, columnWeight)
    )

    // TODO - Extract to values xml
    private fun getTimeHeight(context: Context): Int {
        val dp = 40
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}