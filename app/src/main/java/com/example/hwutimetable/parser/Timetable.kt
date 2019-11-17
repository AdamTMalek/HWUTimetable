package com.example.hwutimetable.parser


class Timetable(
    val hash: ByteArray,
    val days: Array<TimetableDay>
) {
    fun getTotalItems(): Int {
        var items = 0
        days.forEach {day ->
            items += day.items.size
        }
        return items
    }
}