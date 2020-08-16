package com.github.hwutimetable

import android.view.View
import android.widget.TextView
import com.github.hwutimetable.ClassInfoViewPopulator.populateView
import com.github.hwutimetable.parser.TimetableClass

/**
 * The [ClassInfoViewPopulator] is a helper class providing a single method
 * [populateView] which takes a [View] as a parameter and populates it with
 * information about a [TimetableClass]
 */
object ClassInfoViewPopulator {
    /**
     * Populates [view] with all information from [timetableClass], apart from start
     * and end times.
     */
    fun populateView(view: View, timetableClass: TimetableClass) {
        with(view) {
            findViewById<TextView>(R.id.class_code)?.text = timetableClass.code
            findViewById<TextView>(R.id.class_weeks)?.text = timetableClass.weeks.toString()
            findViewById<TextView>(R.id.class_room)?.text = timetableClass.room
            findViewById<TextView>(R.id.class_name)?.text = timetableClass.name
            findViewById<TextView>(R.id.class_lecturer)?.text = timetableClass.lecturer
            findViewById<TextView>(R.id.class_type)?.text = timetableClass.type.name
            findViewById<TextView>(R.id.item_start_time)?.text = timetableClass.start.toString("HH:mm")
            findViewById<TextView>(R.id.item_end_time)?.text = timetableClass.end.toString("HH:mm")
        }
    }
}