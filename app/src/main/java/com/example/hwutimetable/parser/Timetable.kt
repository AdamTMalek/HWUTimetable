package com.example.hwutimetable.parser

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
class Timetable(val hash: ByteArray, val days: Array<TimetableDay>, val semester: Semester) : Parcelable {

    /**
     * Get total items in the timetable (includes clashes - if they exists
     */
    fun getTotalItems() = days.sumBy { it.items.size }

    /**
     * For the given week, the algorithm finds if there are any clashes between items
     * @return [Clashes] which may or may not contain clashes
     */
    fun getClashes(week: Int): Clashes {
        val weekToDisplay = getWeek(week)

        val clashes = Clashes()
        days.forEach { day ->
            clashes.addClashes(day.getClashes(weekToDisplay))
        }

        return clashes
    }

    /**
     * Generates a timetable for the given week.
     * Using this method the timetable will filter out items (lectures, labs etc.) that only happen in the given week.
     * @return [Timetable] for the given week.
     */
    fun getForWeek(week: Int): Timetable {
        val weekToDisplay = getWeek(week)

        val days = mutableListOf<TimetableDay>()
        this.days.forEach { day ->
            days.add(day.getForWeek(weekToDisplay))
        }

        return Timetable(this.hash, days.toTypedArray(), this.semester)
    }

    /**
     * Get the displayable week by the timetable.
     * If the passed week is within the range [1,12] then the same week will be returned as the one passed.
     * Otherwise, if it's out of range then the method will return the lower boundary (1) if the week is
     * less than 1, and upper boundary if the week is greater than 12.
     * @return [Int] between 1 and 12
     */
    private fun getWeek(week: Int): Int {
        return when {
            week <= 1 -> 1
            week >= 12 -> 12
            else -> week
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Timetable

        if (!hash.contentEquals(other.hash)) return false
        if (!days.contentEquals(other.days)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hash.contentHashCode()
        result = 31 * result + days.contentHashCode()
        return result
    }

}