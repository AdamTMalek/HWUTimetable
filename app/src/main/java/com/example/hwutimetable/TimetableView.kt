package com.example.hwutimetable

import android.annotation.SuppressLint
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

    /**
     * Constructs the [ScrollView] with all the [timetable] information inserted
     * @return [ScrollView] with the timetable items
     */
    fun getTimetableItemView(context: Context, timetable: TimetableDay): ScrollView {
        val scrollView = createScrollView(context)
        val gridLayout = createMainGridLayout(context)
        addHourLabels(context, gridLayout)
        addItems(context, gridLayout, timetable)
        scrollView.addView(gridLayout)
        return scrollView
    }

    /**
     * Creates and adds hour labels to the given [gridLayout]
     * @param gridLayout: [GridLayout] created by [createMainGridLayout]
     */
    private fun addHourLabels(context: Context, gridLayout: GridLayout) {
        // From 9:15 till 18:00 there are 36 labels
        for (i in 0..36) {
            val text = getHourLabelText(i)
            gridLayout.addView(
                createTimeTextView(context, text),
                getLayoutParams(i, 0, columnWeight = 0.2f)
            )
        }
    }

    /**
     * Using the given [index] get the time as [String] to be displayed in the label.
     * For instance, if the [index] is 0, the method will return "9:15",
     * for [index] of 1, the returned value will be "9:30"
     * @param index: Row index of the label
     * @return Time that the label should display as [String]
     */
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

    /**
     * Creates and adds timetable items to the given [gridLayout]
     * @param gridLayout: Grid layout created by [createMainGridLayout]
     * @param timetable: Timetable to display
     */
    private fun addItems(context: Context, gridLayout: GridLayout, timetable: TimetableDay) {
        for (item in timetable.items) {
            val row = getRowIndexByTime(item.start)
            val rowspan = getRowspanByPeriod(item.duration)
            gridLayout.addView(
                createItem(context, item),
                getLayoutParams(row, 1, columnWeight = 0.8f, rowSpan = rowspan)
            )
        }
    }

    /**
     * Creates one timetable item
     * @return [LinearLayout] object with children that represent the item
     */
    @SuppressLint("RtlHardcoded")  // We want to keep positioning irrespectively of locales
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

            // Spread the item name across the whole row
            addView(name, getLayoutParams(1, 0, columnSpan = 3))

            addView(lecturer, getLayoutParams(2, 0, columnWeight = 0.5f))
            addView(type, getLayoutParams(2, 2, columnSpan = 1, columnWeight = 0.5f))

        }

        linearLayout.addView(gridLayout)
        return linearLayout
    }

    /**
     * Creates scroll view. This is the most outer container of the view.
     */
    private fun createScrollView(context: Context): ScrollView {
        return ScrollView(context).also {
            it.id = View.generateViewId()
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    /**
     * Creates grid layout for the whole timetable (time labels + items).
     * This grid layout will go inside a [ScrollView] object that will be
     * created by [createScrollView] method.
     */
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

    /**
     * Creates time text view (label)
     */
    private fun createTimeTextView(context: Context, text: String): TextView {
        return TextView(context).also {
            it.id = View.generateViewId()
            it.text = text
            it.width = 0
            it.height = getTimeLabelHeight(context)
        }
    }

    /**
     * Creates linear layout that acts as a container for the
     * grid layout that created by [createItemGridLayout].
     * @param color: Background color of this linear layout
     */
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

    /**
     * Creates grid layout for a timetable item.
     * Inside the grid layout there should text views created by [createItemTextView]
     * @return 3x3 [GridLayout]
     */
    private fun createItemGridLayout(context: Context): GridLayout {
        return GridLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                getTimeLabelHeight(context) * 4  // spreads the information across one full hour
            )

            orientation = GridLayout.HORIZONTAL
            columnCount = 3
            rowCount = 3
        }
    }

    /**
     * Creates [TextView] object for an item
     * @param text: Text to appear in the text view
     * @param gravity: Gravity of the text view
     */
    private fun createItemTextView(context: Context, text: String, gravity: Int): TextView {
        return TextView(context).apply {
            this.text = text
            this.height = getTimeLabelHeight(context)
            this.width = 0
            this.gravity = gravity
        }
    }

    /**
     * Gets GridLayout parameters
     * @param row: Row index
     * @param column: Column Index
     * @param rowWeight: Optional (1f by default), weight of the row
     * @param columnWeight: Optional (1f by default), weight of the column
     * @param rowSpan: Equivalent to the stop argument of [GridLayout.spec]
     * @param columnSpan: Equivalent to the stop argument of [GridLayout.spec]
     */
    private fun getLayoutParams(row: Int, column: Int,
                                rowWeight: Float = 1f,
                                columnWeight: Float = 1f,
                                rowSpan: Int = 1,
                                columnSpan: Int = 1) = GridLayout.LayoutParams(
        GridLayout.spec(row, rowSpan, rowWeight),
        GridLayout.spec(column, columnSpan, columnWeight)
    )

    /**
     * Gets the time label height in pixels from the dimensions resources
     * @return Label height in pixels
     */
    private fun getTimeLabelHeight(context: Context): Int {
        return context.resources.getDimensionPixelSize(R.dimen.hour_label_height)
    }

    /**
     * Calculates the index at which the item should be placed inside timetable grid layout.
     * @param localTime: [LocalTime] object representing the time at which the timetable item starts
     * @return: Row index
     */
    private fun getRowIndexByTime(localTime: LocalTime): Int {
        val dayStart = Period(9, 15, 0, 0)
        val itemStart = Period(localTime.hourOfDay, localTime.minuteOfHour, 0, 0)
        val differenceMinutes = itemStart.minus(dayStart).toStandardMinutes().minutes
        return differenceMinutes / 15
    }

    /**
     * Calculates the rowspan using the given period
     * @param: [Period] should be the duration of a timetable item.
     * @return: Row span
     */
    private fun getRowspanByPeriod(period: Period): Int {
        return period.minutes / 15
    }
}