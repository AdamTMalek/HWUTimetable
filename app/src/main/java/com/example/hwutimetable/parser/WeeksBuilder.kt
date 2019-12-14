package com.example.hwutimetable.parser

import com.example.hwutimetable.extensions.toIntArray
import com.example.hwutimetable.parser.exceptions.InvalidRangeException

class WeeksBuilder {
    private var weeks: IntArray = IntArray(0)

    /**
     * Get the number of weeks in the specified weeks
     */
    fun getNumberOfWeeks(): Weeks {
        return Weeks(weeks)
    }

    /**
     * Set weeks based on the given range
     * @throws IllegalArgumentException when start or end week is out of range
     * @throws InvalidRangeException when start week is greater than end week
     */
    @Throws(IllegalArgumentException::class, InvalidRangeException::class)
    fun setRange(start: Int, end: Int): WeeksBuilder {
        if (!isValidWeek(start))
            throw IllegalArgumentException("range start must be between 1 and 12")
        if (!isValidWeek(end))
            throw IllegalArgumentException("range end must be between 1 and 12")
        if (start > end)
            throw InvalidRangeException("start must be lower than end")

        this.weeks = (start..end).toIntArray()
        return this
    }

    /**
     * Set weeks based on integer array ([IntArray])
     * @throws IllegalArgumentException when any of the specified weeks is out of range
     */
    @Throws(IllegalArgumentException::class)
    fun setWeeks(weeks: IntArray): WeeksBuilder {
        weeks.forEach {
            if (!isValidWeek(it))
                throw IllegalArgumentException("Week $it is not a valid week (out of range)")
        }

        this.weeks = weeks
        return this
    }

    /**
     * Set the weeks data by parsing the string
     * The string can be:
     * - a range "1-12" or "2-6"
     * - a single week "7"
     * - a list of weeks "1,2,3,4"
     * @throws IllegalArgumentException when a parsed week is not a valid week (out of range)
     * @throws InvalidRangeException when in specified range the start week is greater than end week
     */
    @Throws(IllegalArgumentException::class, InvalidRangeException::class)
    fun setFromString(string: String): WeeksBuilder {
        if (stringHasRange(string)) {
            val (start, end) = parseRange(string)
            setRange(start, end)
        } else {
            val weeks = parseList(string)
            setWeeks(weeks)
        }

        return this
    }

    private fun isValidWeek(week: Int) = week in 1..12

    /**
     * Parse the given string as range ("{start}-{end}")
     * @return start and end pair
     */
    private fun parseRange(string: String): Pair<Int, Int> {
        val regex = Regex("(\\d+)")
        val result = regex.findAll(string)

        val start = result.first().value.toInt()
        val end = result.last().value.toInt()
        return Pair(start, end)
    }

    /**
     * Parse the given string as list ("{w1, w2, w3}")
     * @return Parsed weeks int array
     */
    private fun parseList(string: String): IntArray {
        val regex = Regex("(\\d+)")
        val result = regex.findAll(string)

        val weeks = result.map { it.value.toInt() }.toList()
        return weeks.toIntArray()
    }

    private fun stringHasRange(string: String) = string.contains('-')
}