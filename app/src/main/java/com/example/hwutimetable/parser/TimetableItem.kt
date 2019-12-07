package com.example.hwutimetable.parser

import android.os.Parcel
import android.os.Parcelable
import org.joda.time.LocalTime
import org.joda.time.Minutes
import org.joda.time.Period

/**
 * TimetableItem object represents a lecture, lab, tutorial etc.
 */
open class TimetableItem(
    val code: String,
    val name: String,
    val room: String,
    val lecturer: String,
    val type: ItemType,
    val start: LocalTime,
    val end: LocalTime,
    val weeks: String,
    val duration: Period = Period.minutes(Minutes.minutesBetween(start, end).minutes)) : Parcelable {

    constructor(parcel: Parcel) : this(
        code = parcel.readString()!!,
        name = parcel.readString()!!,
        room = parcel.readString()!!,
        lecturer = parcel.readString()!!,
        type = ItemType(parcel.readString()!!),
        start = LocalTime.parse(parcel.readString()),
        end = LocalTime.parse(parcel.readString()),
        weeks = parcel.readString()!!
    )

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest ?: return
        with(dest) {
            writeString(code)
            writeString(name)
            writeString(room)
            writeString(lecturer)
            writeString(type.name)
            writeString(start.toString())
            writeString(end.toString())
            writeString(weeks)
        }
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<TimetableItem> {
        override fun createFromParcel(parcel: Parcel): TimetableItem {
            return TimetableItem(parcel)
        }

        override fun newArray(size: Int): Array<TimetableItem?> {
            return arrayOfNulls(size)
        }
    }
}
