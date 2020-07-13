package com.github.hwutimetable.parser

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.joda.time.LocalDate
import org.joda.time.Weeks
import java.io.Serializable


@Parcelize
data class Semester(val startDate: LocalDate, val number: Int) : Parcelable, Serializable {
    /**
     * Get the semester week from the given date.
     * If the passed date is before the start date of the semester then the method will return 1.
     * If the passed date is past the end date of the semester (week 12) then 12 will be returned.
     * @return [Int] between 1 and 12
     */
    fun getWeek(date: LocalDate): Int {
        val weeks = Weeks.weeksBetween(startDate, date).weeks + 1
        return when {
            weeks <= 1 -> 1
            weeks >= 12 -> 12
            else -> weeks
        }
    }

}