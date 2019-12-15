package com.example.hwutimetable.parser

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.joda.time.LocalDate
import org.joda.time.Weeks


@Parcelize
class Semester(val startDate: LocalDate) : Parcelable {
    fun getCurrentWeek(currentDate: LocalDate): Int {
        return Weeks.weeksBetween(startDate, currentDate).weeks + 1
    }
}