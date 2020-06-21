package com.example.hwutimetable

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.example.hwutimetable.TimetableView.createItemLinearLayout
import com.example.hwutimetable.TimetableView.createMainGridLayout
import com.example.hwutimetable.TimetableView.createScrollView
import com.example.hwutimetable.parser.TimetableDay
import com.example.hwutimetable.parser.TimetableItem
import org.joda.time.LocalTime
import org.joda.time.Period


/**
 * The TimetableView object is a singleton that constructs the whole view
 * for the given timetable ([TimetableDay]).
 *
 * The view as a whole is constructed of many views nested in each other.
 * The main outer view is a [ScrollView] created by [createScrollView].
 * This view has only one child which is created by [createMainGridLayout].
 * The grid layout has 36 rows and 2 columns. First column contains hour labels
 * and the second timetable items if they exist at the given hour, otherwise
 * cell (at the 2nd column) of the row of the corresponding hour is left empty.
 *
 * If there is an timetable item for the given hour it is made up of multiple
 * views to resemble the same layout as on the original web timetables
 * website, while also making it mobile friendly and readable.
 *
 * An item is made up of [LinearLayout] created by [createItemLinearLayout]
 * this creates the background for the item. Inside it there's a
 * 3 by 3 [GridLayout] holding all TextViews
 * that contain information regarding the item (type, lecturer, room etc.).
 */
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
        val linearLayout = createItemLinearLayout(context, item.type.getBackground(context))
        val inflater = LayoutInflater.from(context)
        val gridLayout = inflater.inflate(R.layout.timetable_item, linearLayout, false)

        with(gridLayout) {
            findViewById<TextView>(R.id.item_code).text = item.code
            findViewById<TextView>(R.id.item_weeks).text = item.weeks.toString()
            findViewById<TextView>(R.id.item_room).text = item.room
            findViewById<TextView>(R.id.item_name).text = item.name
            findViewById<TextView>(R.id.item_lecturer).text = item.lecturer
            findViewById<TextView>(R.id.item_type).text = item.type.name
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
     * Creates linear layout that acts as a container for the grid layout.
     * @param background: [Drawable] background of the item
     */
    private fun createItemLinearLayout(context: Context, background: Drawable): LinearLayout {
        return LinearLayout(context).also {
            it.id = View.generateViewId()
            it.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            it.gravity = Gravity.CENTER_VERTICAL
            it.background = background
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
    private fun getLayoutParams(
        row: Int, column: Int,
        rowWeight: Float = 1f,
        columnWeight: Float = 1f,
        rowSpan: Int = 1,
        columnSpan: Int = 1
    ) = GridLayout.LayoutParams(
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