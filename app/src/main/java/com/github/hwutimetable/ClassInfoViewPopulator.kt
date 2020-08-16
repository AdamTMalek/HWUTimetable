package com.github.hwutimetable

import android.view.View
import android.widget.TextView
import com.github.hwutimetable.ClassInfoViewPopulator.populateView
import com.github.hwutimetable.parser.TimetableItem

/**
 * The [ClassInfoViewPopulator] is a helper class providing a single method
 * [populateView] which takes a [View] as a parameter and populates it with
 * information about a [TimetableItem]
 */
object ClassInfoViewPopulator {
    /**
     * Populates [view] with all information from [item], apart from start
     * and end times.
     */
    fun populateView(view: View, item: TimetableItem) {
        with(view) {
            findViewById<TextView>(R.id.item_code)?.text = item.code
            findViewById<TextView>(R.id.item_weeks)?.text = item.weeks.toString()
            findViewById<TextView>(R.id.item_room)?.text = item.room
            findViewById<TextView>(R.id.item_name)?.text = item.name
            findViewById<TextView>(R.id.item_lecturer)?.text = item.lecturer
            findViewById<TextView>(R.id.item_type)?.text = item.type.name
            findViewById<TextView>(R.id.item_start_time)?.text = item.start.toString("HH:mm")
            findViewById<TextView>(R.id.item_end_time)?.text = item.end.toString("HH:mm")
        }
    }
}