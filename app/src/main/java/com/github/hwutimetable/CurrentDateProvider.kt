package com.github.hwutimetable

import org.joda.time.LocalDate

interface CurrentDateProvider {
    fun getCurrentDate(): LocalDate
}