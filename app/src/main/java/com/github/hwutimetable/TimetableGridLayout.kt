package com.github.hwutimetable

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.preference.PreferenceManager
import com.github.hwutimetable.parser.TimetableDay
import com.github.hwutimetable.parser.TimetableItem
import org.joda.time.LocalTime
import org.joda.time.Period

/**
 * The [TimetableGridLayout] is a [GridLayout] for displaying
 * a single day from the whole timetable timetable.
 * To use it, first populate the timetable into the grid layout by using
 * [addTimetableDay] passing [TimetableDay] as an argument.
 */
class TimetableGridLayout(context: Context) : GridLayout(context) {
    private val emptyRows: MutableList<Int>

    init {
        id = R.id.timetable_grid
        columnCount = 2
        rowCount = 36
        orientation = HORIZONTAL
        useDefaultMargins = true
        emptyRows = (0 until rowCount).toMutableList()
        setLayoutParams()
        addHourLabels()
    }

    private fun setLayoutParams() {
        val sideMargins = context.resources.getDimensionPixelSize(R.dimen.timetable_left_margin)
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ).apply { leftMargin = sideMargins; rightMargin = sideMargins }
    }

    fun addTimetableDay(timetable: TimetableDay) {
        timetable.items.forEach { addTimetableItem(it) }
        fillBlankCells()
    }

    private fun addTimetableItem(timetableItem: TimetableItem) {
        val itemView = createViewForItem(timetableItem)
        val row = getRowIndexByTime(timetableItem.start)
        val rowSpan = getRowspanByPeriod(timetableItem.duration)
        addView(itemView, createLayoutParams(row, 1, columnWeight = 0.7f, rowSpan = rowSpan))

        val lastRow = row + rowSpan - 1
        emptyRows.removeAll { it in (row..lastRow) }
        addItemClickHandler(itemView, timetableItem)
    }

    /**
     * Creates one timetable item
     * @return [LinearLayout] object with children that represent the item
     */
    @SuppressLint("RtlHardcoded")  // We want to keep positioning irrespectively of locales
    private fun createViewForItem(item: TimetableItem): LinearLayout {
        val linearLayout = createItemLinearLayout(item.type.getBackground(context))
        val itemView = if (useSimplifiedViewForItem())
            createSimpleViewForItem(item, linearLayout)
        else
            createOriginalViewForItem(item, linearLayout)

        linearLayout.addView(itemView)
        return linearLayout
    }

    private fun useSimplifiedViewForItem() = PreferenceManager.getDefaultSharedPreferences(context)
        .getBoolean(context.getString(R.string.use_simplified_view), false)

    private fun createOriginalViewForItem(item: TimetableItem, linearLayout: LinearLayout): View {
        return createViewForItem(item, linearLayout, R.layout.timetable_item_original)
    }

    private fun createSimpleViewForItem(item: TimetableItem, linearLayout: LinearLayout): View {
        return createViewForItem(item, linearLayout, R.layout.timetable_item_simple)
    }

    private fun createViewForItem(item: TimetableItem, linearLayout: LinearLayout, resource: Int): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(resource, linearLayout, false)
        ClassInfoViewPopulator.populateView(view, item)
        return view
    }

    /**
     * Creates linear layout that acts as a container for the grid layout.
     * @param background: [Drawable] background of the item
     */
    private fun createItemLinearLayout(background: Drawable): LinearLayout {
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

    private fun addItemClickHandler(itemView: View, item: TimetableItem) {
        itemView.setOnClickListener {
            ClassInfoPopupWindow.create(context, item)
                .showAtLocation(this, Gravity.CENTER, 0, 0)
        }
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

    private fun addHourLabels() {
        for (i in 0 until rowCount) {
            val text = getHourLabelText(i)
            addView(
                createTimeTextView(text),
                createLayoutParams(i, 0, columnWeight = 0.3f)
            )
        }
    }

    /**
     * Calculates the rowspan using the given period
     * @param: [Period] should be the duration of a timetable item.
     * @return: Row span
     */
    private fun getRowspanByPeriod(period: Period): Int {
        return period.minutes / 15
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
     * Creates time text view (label)
     */
    private fun createTimeTextView(text: String): TextView {
        return TextView(context).also {
            it.id = View.generateViewId()
            it.text = text
            it.width = 0
            it.height = getTimeLabelHeight()
            it.background = context.resources.getDrawable(R.drawable.top_line, context.theme)
        }
    }

    /**
     * Gets the time label height in pixels from the dimensions resources
     * @return Label height in pixels
     */
    private fun getTimeLabelHeight(): Int {
        return context.resources.getDimensionPixelSize(R.dimen.hour_label_height)
    }

    /**
     * Creates GridLayout parameters with 0 margins
     * @param row: Row index
     * @param column: Column Index
     * @param rowWeight: Optional (1f by default), weight of the row
     * @param columnWeight: Optional (1f by default), weight of the column
     * @param rowSpan: Equivalent to the stop argument of [GridLayout.spec]
     * @param columnSpan: Equivalent to the stop argument of [GridLayout.spec]
     */
    private fun createLayoutParams(
        row: Int, column: Int,
        rowWeight: Float = 1f,
        columnWeight: Float = 1f,
        rowSpan: Int = 1,
        columnSpan: Int = 1
    ) = LayoutParams(
        spec(row, rowSpan, rowWeight),
        spec(column, columnSpan, columnWeight)
    ).apply {
        leftMargin = 0
        rightMargin = 0
        bottomMargin = 0
        topMargin = 0
    }

    /**
     * Goes through each empty row and adds an empty linear layout with the top line background
     * which acts as a separator for each hour.
     */
    private fun fillBlankCells() {
        emptyRows.forEach { row ->
            val layoutParams = createLayoutParams(row, 1, columnWeight = 0.8f, rowSpan = 1)
            addView(
                createItemLinearLayout(
                    context.resources.getDrawable(R.drawable.top_line, context.theme)
                ), layoutParams
            )
        }
    }

    /**
     * Returns a sequence of the timetable items that have been added to the layout.
     */
    fun getTimetableItems() = children.filter {
        (it is LinearLayout) && (it.findViewById<TextView>(R.id.item_code) != null)
    }
}