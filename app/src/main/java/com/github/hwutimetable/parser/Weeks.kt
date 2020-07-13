package com.github.hwutimetable.parser

data class Weeks(val weeks: IntArray) {
    /**
     * Get the duration (number of weeks)
     */
    fun getNumberOfWeeks() = weeks.size

    /**
     * Check if there are any common weeks in this weeks object and the one passed to the function
     * @return true if there is/are common week(s)
     */
    fun hasCommon(weeks: Weeks) = getCommon(weeks).isNotEmpty()

    /**
     * Return common weeks appearing in this object and the one passed to the function
     * @return Weeks that are common in both objects
     */
    fun getCommon(weeks: Weeks) = this.weeks.intersect(weeks.weeks.toList())

    /**
     * Check if the weeks contains the given week
     * @return True if it contains the given week
     */
    fun contains(week: Int) = weeks.contains(week)

    /**
     * If weeks are rangeable we can represent them with hyphen instead of listing every week
     * @return true if rangeable
     */
    private fun isRangeable(): Boolean {
        val last = weeks.last()
        val first = weeks.first()

        return (last - first + 1) == weeks.size
    }

    override fun toString(): String {
        return if (isRangeable()) {
            "${weeks.first()}-${weeks.last()}"
        } else {
            weeks.joinToString(prefix = "", postfix = "", separator = ",")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Weeks

        if (!weeks.contentEquals(other.weeks)) return false

        return true
    }

    override fun hashCode(): Int {
        return weeks.contentHashCode()
    }
}