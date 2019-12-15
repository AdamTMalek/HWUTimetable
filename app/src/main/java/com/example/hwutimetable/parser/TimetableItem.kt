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
    val weeks: Weeks
) : Parcelable {

    val duration: Period = Period.minutes(Minutes.minutesBetween(start, end).minutes)

    constructor(parcel: Parcel) : this(
        code = parcel.readString()!!,
        name = parcel.readString()!!,
        room = parcel.readString()!!,
        lecturer = parcel.readString()!!,
        type = ItemType(parcel.readString()!!),
        start = LocalTime.parse(parcel.readString()),
        end = LocalTime.parse(parcel.readString()),
        weeks = WeeksBuilder()
            .setFromString(parcel.readString()!!)
            .getWeeks()
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
            writeString(weeks.toString())
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TimetableItem

        if (code != other.code) return false
        if (name != other.name) return false
        if (room != other.room) return false
        if (lecturer != other.lecturer) return false
        if (type != other.type) return false
        if (start != other.start) return false
        if (end != other.end) return false
        if (weeks != other.weeks) return false
        if (duration != other.duration) return false

        return true
    }

    override fun hashCode(): Int {
        var result = code.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + room.hashCode()
        result = 31 * result + lecturer.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + start.hashCode()
        result = 31 * result + end.hashCode()
        result = 31 * result + weeks.hashCode()
        result = 31 * result + duration.hashCode()
        return result
    }
}
