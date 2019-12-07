package com.example.hwutimetable.parser


class Timetable(val hash: ByteArray, val days: Array<TimetableDay>) {
    fun getTotalItems() = days.sumBy { it.items.size }
}