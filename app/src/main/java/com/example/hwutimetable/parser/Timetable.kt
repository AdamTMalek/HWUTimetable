package com.example.hwutimetable.parser

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
class Timetable(val hash: ByteArray, val days: Array<TimetableDay>, val semester: Semester) : Parcelable {
    fun getTotalItems() = days.sumBy { it.items.size }

    fun getClashes(): Clashes {
        val clashes = Clashes()
        // TODO: Find current week and decide if there's a clash during the week
        val week = 1
        days.forEach { day ->
            clashes.addClashes(day.getClashes(week))
        }
        return clashes
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Timetable

        if (!hash.contentEquals(other.hash)) return false
        if (!days.contentEquals(other.days)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hash.contentHashCode()
        result = 31 * result + days.contentHashCode()
        return result
    }

}