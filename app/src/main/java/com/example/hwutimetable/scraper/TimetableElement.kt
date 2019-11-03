package com.example.hwutimetable.scraper

import org.joda.time.LocalTime

/**
 * An enum class that represents a type of a TimetableElement
 * For instance, a lecture, a tutorial or a lab etc.
 */
enum class TimetableElementType(val type: String) {
    LEC("Lecture"),
    WKP("Work Project"),
    CLAB("Computer Lab"),
    TUT("Tutorial"),;

    override fun toString(): String {
        return this.type
    }
}

/**
 * Timetable element represents a single element in a timetable
 * Such element can be a lecture, a tutorial or a lab.
 * It's type is denoted by the type val which is of type ElementType
 * @constructor Map with all but duration information about the element
 */
data class TimetableElement(val map: Map<String, Any>) {
    val code: String
    val name: String
    val room: String
    val lecturer : String
    val type : TimetableElementType
    val start: LocalTime
    val end: LocalTime
    val duration: LocalTime

    init {
        with(map) {
            code = get("code") as String
            name = get("name") as String
            room = get("room") as String
            lecturer = get("lecturer") as String
            type = get("type") as TimetableElementType
            start = get("start") as LocalTime
            end = get("end") as LocalTime
            duration = end.minusHours(start.hourOfDay).minusMinutes(start.minuteOfHour)
        }
    }

    override fun toString(): String {
        return "$code $name from ${start.toString("HH:mm")} to ${end.toString("HH:mm")} in $room"
    }
}
