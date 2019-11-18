package com.example.hwutimetable.parser

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import org.joda.time.LocalTime
import org.joda.time.Minutes
import org.joda.time.Period

/**
 * TimetableItem object represents a lecture, lab, tutorial etc.
 */
@Parcelize
data class TimetableItem(
    val code: String,
    val name: String,
    val room: String,
    val lecturer: String,
    val type: @RawValue ItemType,
    val start: LocalTime,
    val end: LocalTime,
    val weeks: String,
    val duration: Period = Period.minutes(Minutes.minutesBetween(start, end).minutes)) : Parcelable {
}