package com.example.hwutimetable.parser

import org.joda.time.LocalTime
import org.joda.time.Minutes
import org.joda.time.Period

/**
 * Lecture object represents a lecture, lab, tutorial etc.
 */
data class Lecture(
    val code: String,
    val name: String,
    val room: String,
    val lecturer: String,
    val type: String,
    val start: LocalTime,
    val end: LocalTime,
    val weeks: String) {

    val duration: Period = Period.minutes(Minutes.minutesBetween(start, end).minutes)

}