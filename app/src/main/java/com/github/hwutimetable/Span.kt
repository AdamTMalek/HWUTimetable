package com.github.hwutimetable

import org.joda.time.Period


object Span {
    /**
     * Get the minutes of the given [period] and return the equivalent
     * row span for the timetable
     */
    fun getSpanFromPeriod(period: Period) = period.minutes / 15
}