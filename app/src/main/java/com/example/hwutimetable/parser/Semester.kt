package com.example.hwutimetable.parser

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.joda.time.LocalDate
import org.joda.time.Weeks
import java.io.Serializable


@Parcelize
class Semester(val startDate: LocalDate) : Parcelable, Serializable {
    fun getCurrentWeek(currentDate: LocalDate): Int {
        return Weeks.weeksBetween(startDate, currentDate).weeks + 1
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Semester

        if (startDate != other.startDate) return false

        return true
    }

    override fun hashCode(): Int {
        return startDate.hashCode()
    }

}