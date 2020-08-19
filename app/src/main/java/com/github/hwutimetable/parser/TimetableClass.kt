package com.github.hwutimetable.parser

import android.os.Parcel
import android.os.Parcelable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.LocalTime
import org.joda.time.Minutes
import org.joda.time.Period
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

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
    data class Type(val name: String, val color: String) {
        interface BackgroundProvider {
            suspend fun getBackgroundColor(type: String): String
        }
        /**
         * The [OnlineBackgroundProvider] can fetch a file from the given [url] and parse it to get the
         * appropriate color for the activity/class type.
         */
        class OnlineBackgroundProvider(private val url: URL = URL("https://timetable.hw.ac.uk/WebTimetables/LiveED/activitytype.css"))
            : BackgroundProvider {
            private val colorRegex = Regex("background-color: (#[a-zA-Z0-9]+)")
            private lateinit var cssCopy: BufferedReader

            private suspend fun fetchCss(): BufferedReader {
                if (this::cssCopy.isInitialized)
                    return cssCopy

                return withContext(Dispatchers.IO) {
                    val stream = url.openStream()
                    BufferedReader(InputStreamReader(stream))
                }
            }

            /**
             * Returns the background color by fetching the CSS file from the server
             * and looks for the correct class (type).
             */
            override suspend fun getBackgroundColor(type: String): String {
                val classType = ".${type.replace(' ', '_')}"
                return fetchCss().useLines { lines ->
                    lines.forEach { line ->
                        if (line.startsWith(classType, ignoreCase = true)) {
                            return@useLines colorRegex.find(line)!!.groupValues[1]
                        }
                    }
                    throw NoSuchTypeException(classType)
                }
            }
        }

        class NoSuchTypeException(type: String) : Exception("Could not find the background for class: $type")
    }

    val duration: Period = Period.minutes(Minutes.minutesBetween(start, end).minutes)

    constructor(parcel: Parcel) : this(
        code = parcel.readString()!!,
        name = parcel.readString()!!,
        room = parcel.readString()!!,
        lecturer = parcel.readString()!!,
        type = Type(parcel.readString()!!, parcel.readString()!!),
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
            writeString(type.color)
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
