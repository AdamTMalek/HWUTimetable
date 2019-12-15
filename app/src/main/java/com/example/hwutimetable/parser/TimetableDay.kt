package com.example.hwutimetable.parser

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
            val clash = getClash(i, week)
            if (clash != null)
                clashes.addClash(clash)
        }

        return clashes
    }

    /**
     * Finds a clash for the item at index [index] for the given [week]
     * @return [Clash] if a clash exists, null otherwise.
     */
    private fun getClash(index: Int, week: Int): Clash? {
        val item = items[index]

        for (i in (index + 1) until items.size) {
            val itemToCompare = items[i]

            // Check if there is a time overlap
            if (Minutes.minutesBetween(item.end, itemToCompare.start).minutes >= 0)
                continue // If there's no overlap - continue, there won't be any clash

            val commonWeeks = item.weeks.getCommon(itemToCompare.weeks)
            if (commonWeeks.isNotEmpty() && !commonWeeks.contains(week))
                return Clash(this.day, item, itemToCompare)
        }

        return null
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