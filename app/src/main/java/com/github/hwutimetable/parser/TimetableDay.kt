package com.github.hwutimetable.parser

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.joda.time.Minutes

enum class Day(val index: Int) {
    MONDAY(0),
    TUESDAY(1),
    WEDNESDAY(2),
    THURSDAY(3),
    FRIDAY(4);

    override fun toString(): String {
        return when (this) {
            MONDAY -> "Monday"
            TUESDAY -> "Tuesday"
            WEDNESDAY -> "Wednesday"
            THURSDAY -> "Thursday"
            FRIDAY -> "Friday"
        }
    }
}

@Parcelize
data class TimetableDay(val day: Day, val classes: ArrayList<TimetableClass>) : Parcelable {

    /**
     * Get new object that contains only timetable classes that happen in the given week
     */
    fun getForWeek(week: Int): TimetableDay {
        return TimetableDay(
            this.day,
            ArrayList(classes.filter { item -> item.weeks.contains(week) })
        )
    }

    /**
     * Get clashes (if they exist) for the given week
     */
    fun getClashes(week: Int): Clashes {
        val clashes = Clashes()
        for (i in 0 until (classes.size - 1)) {
            clashes.addClashes(getClashes(i, week))
        }
        return clashes
    }

    /**
     * Finds a clash for the item at index [index] for the given [week]
     * @return [Clash] if a clash exists, null otherwise.
     */
    private fun getClashes(index: Int, week: Int): Clashes {
        val item = classes[index]
        val clashes = Clashes()
        for (i in (index + 1) until classes.size) {
            val itemToCompare = classes[i]

            if (!overlapExists(item, itemToCompare))
                continue // If there's no overlap - continue

            val commonWeeks = item.weeks.getCommon(itemToCompare.weeks)
            if (commonWeeks.contains(week))
                clashes.addClash(Clash(this.day, item, itemToCompare))
        }

        return clashes
    }

    /**
     * Checks if there is an overlap between class1 and class2.
     * @return true if overlap exists, false otherwise.
     */
    private fun overlapExists(class1: TimetableClass, class2: TimetableClass): Boolean {
        // Find the minutes between the end of item 1 and start of item 2
        var minutes = Minutes.minutesBetween(class1.end, class2.start).minutes
        if (minutes >= 0)
            return false  // No overlap

        // Now, we know that item 2 start comes before the end of item 1
        // We need to check if item 2 finishes before item 1 starts.
        // We do exactly the same as before, except swap item 1 and item 2
        minutes = Minutes.minutesBetween(class2.end, class1.start).minutes
        if (minutes >= 0)
            return false

        // Here, we know that overlap exists
        return true
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TimetableDay

        if (day != other.day) return false
        if (classes != other.classes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = day.hashCode()
        result = 31 * result + classes.hashCode()
        return result
    }
}