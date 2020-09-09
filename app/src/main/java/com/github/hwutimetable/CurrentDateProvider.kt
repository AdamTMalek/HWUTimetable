package com.github.hwutimetable

import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

interface CurrentDateProvider {
    fun getCurrentDate(): LocalDate
    fun getCurrentDateTime(): LocalDateTime
}