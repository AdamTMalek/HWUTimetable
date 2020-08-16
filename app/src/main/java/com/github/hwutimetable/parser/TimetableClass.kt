package com.github.hwutimetable.parser

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import androidx.core.content.ContextCompat
import org.joda.time.LocalTime
import org.joda.time.Minutes
import org.joda.time.Period

/**
 * TimetableClass object represents a lecture, lab, tutorial etc.
 */
data class TimetableClass(
    val code: String,
    val name: String,
    val room: String,
    val lecturer: String,
    val type: Type,
    val start: LocalTime,
    val end: LocalTime,
    val weeks: Weeks
) : Parcelable {
    /**
     * [Type] represents a type of a timetable class.
     * This can be a lecture, tutorial, lab etc.
     * @property name: Type as it appears on the website timetable (e.g. CLab, Tut, Lec)
     */
    data class Type(val name: String) {

        /**
         * Gets the background associated with this item type
         * @return: A background from drawable resources
         */
        fun getBackground(context: Context): Drawable {
            val id = getId(context)
            return ContextCompat.getDrawable(context, id)
                ?: throw Resources.NotFoundException("Failed to load drawable with id $id")
        }

        /**
         * Gets the id of the drawable
         */
        private fun getId(context: Context): Int {
            val name = this.name.toLowerCase()
            val typeName = when (name) {
                "wkp", "sgrp", "plab", "llab" -> "lab"  // These have the same background
                else -> name
            }.plus("_background") // add _background suffix

            val id = context.resources.getIdentifier(typeName, "drawable", context.packageName)

            if (id == 0) {
                throw Resources.NotFoundException("The background with name $name was not found in the resources")
            }

            return id
        }
    }

    val duration: Period = Period.minutes(Minutes.minutesBetween(start, end).minutes)

    constructor(parcel: Parcel) : this(
        code = parcel.readString()!!,
        name = parcel.readString()!!,
        room = parcel.readString()!!,
        lecturer = parcel.readString()!!,
        type = Type(parcel.readString()!!),
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

    companion object CREATOR : Parcelable.Creator<TimetableClass> {
        override fun createFromParcel(parcel: Parcel): TimetableClass {
            return TimetableClass(parcel)
        }

        override fun newArray(size: Int): Array<TimetableClass?> {
            return arrayOfNulls(size)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TimetableClass

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
