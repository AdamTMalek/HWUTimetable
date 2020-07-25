package com.github.hwutimetable

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import com.github.hwutimetable.parser.TimetableDay


/**
 * The TimetableView constructs the whole view for the given timetable ([TimetableDay]).
 *
 * The view as a whole is constructed of many views nested in each other.
 * The main outer view is a [ScrollView] created by [createScrollView].
 * This view has only one child which a [TimetableGridLayout].
 * The grid layout has 36 rows and 2 columns. First column contains hour labels
 * and the second timetable items if they exist at the given hour, otherwise
 * cell (at the 2nd column) of the row of the corresponding hour is left empty.
 *
 * If there is an timetable item for the given hour it is made up of multiple
 * views to resemble the same layout as on the original web timetables
 * website, while also making it mobile friendly and readable.
 *
 * An item is made up of [LinearLayout] created by with an appropriate
 * background for the item. Inside it there's a 3 by 3 [GridLayout]
 * holding all TextViews that contain information regarding the item
 * (type, lecturer, room etc.).
 */
class TimetableViewGenerator(private val context: Context) {
    /**
     * Constructs the [ScrollView] with all the [timetable] information inserted
     * @return [ScrollView] with the timetable items
     */
    fun getTimetableItemView(timetable: TimetableDay): ViewGroup {
        return TimetableGridLayout(context).apply {
            addTimetableDay(timetable)
        }
    }

    /**
     * Creates scroll view. This is the most outer container of the view.
     */
    private fun createScrollView(): ScrollView {
        return ScrollView(context).also {
            it.id = View.generateViewId()
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }
}