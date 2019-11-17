package com.example.hwutimetable.parser

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

enum class Day(val index: Int) {
    MONDAY(0),
    TUESDAY(1),
    WEDNESDAY(2),
    THURSDAY(3),
    FRIDAY(4)
}

@Parcelize
data class TimetableDay(val day: Day, val items: ArrayList<TimetableItem>) : Parcelable
