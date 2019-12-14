package com.example.hwutimetable.parser

import org.joda.time.LocalDate
import org.joda.time.Weeks

class Semester(private val startDate: LocalDate) {
    fun getCurrentWeek(currentDate: LocalDate): Int {
        return Weeks.weeksBetween(startDate, currentDate).weeks + 1
    }
}