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
data class TimetableDay(val day: Day, val items: ArrayList<TimetableItem>) : Parcelable {

    /**
     * Get new object that contains only items that happen in the given week
     */
    fun getForWeek(week: Int): TimetableDay {
        return TimetableDay(
            this.day,
            ArrayList(items.filter { item -> item.weeks.contains(week) })
        )
    }

    /**
     * Get clashes (if they exist) for the given week
     */
    fun getClashes(week: Int): Clashes {
        val clashes = Clashes()
        for (i in 0 until (items.size - 1)) {
            clashes.addClashes(getClashes(i, week))
        }
        return clashes
    }

    /**
     * Finds a clash for the item at index [index] for the given [week]
     * @return [Clash] if a clash exists, null otherwise.
     */
    private fun getClashes(index: Int, week: Int): Clashes {
        val item = items[index]
        val clashes = Clashes()
        for (i in (index + 1) until items.size) {
            val itemToCompare = items[i]

            if (!overlapExists(item, itemToCompare))
                continue // If there's no overlap - continue

            val commonWeeks = item.weeks.getCommon(itemToCompare.weeks)
            if (commonWeeks.contains(week))
                clashes.addClash(Clash(this.day, item, itemToCompare))
        }

        return clashes
    }

    /**
     * Checks if there is an overlap between item1 and item2.
     * @return true if overlap exists, false otherwise.
     */
    private fun overlapExists(item1: TimetableItem, item2: TimetableItem): Boolean {
        // Find the minutes between the end of item 1 and start of item 2
        var minutes = Minutes.minutesBetween(item1.end, item2.start).minutes
        if (minutes >= 0)
            return false  // No overlap

        // Now, we know that item 2 start comes before the end of item 1
        // We need to check if item 2 finishes before item 1 starts.
        // We do exactly the same as before, except swap item 1 and item 2
        minutes = Minutes.minutesBetween(item2.end, item1.start).minutes
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
        if (items != other.items) return false

        return true
    }

    override fun hashCode(): Int {
        var result = day.hashCode()
        result = 31 * result + items.hashCode()
        return result
    }
}