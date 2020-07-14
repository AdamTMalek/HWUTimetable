package com.github.hwutimetable.parser

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Timetable(val days: Array<TimetableDay>, val info: TimetableInfo) : Parcelable {
    /**
     * TimetableInfo represents information about a timetable.
     * @param code: Code of the timetable (from the option value from the Timetables website)
     * @param name: Readable name
     * @param semester: Semester information (start date and number)
     */
    @Parcelize
    data class TimetableInfo(val code: String, val name: String, val semester: Semester) : Parcelable

    /**
     * Get total items in the timetable (includes clashes - if they exists
     */
    fun getTotalItems() = days.sumBy { it.items.size }

    /**
     * For the given week, the algorithm finds if there are any clashes between items
     * @return [Clashes] which may or may not contain clashes
     */
    fun getClashes(week: Int): Clashes {
        val clashes = Clashes()
        days.forEach { day ->
            clashes.addClashes(day.getClashes(week))
        }

        return clashes
    }

    /**
     * Generates a timetable for the given week.
     * Using this method the timetable will filter out items (lectures, labs etc.) that only happen in the given week.
     * @return [Timetable] for the given week.
     */
    fun getForWeek(week: Int): Timetable {
        val days = mutableListOf<TimetableDay>()
        this.days.forEach { day ->
            days.add(day.getForWeek(week))
        }

        return Timetable(days.toTypedArray(), info)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Timetable

        if (info != other.info) return false
        if (!days.contentEquals(other.days)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = days.contentHashCode()
        result = 31 * result + info.hashCode()
        return result
    }
}