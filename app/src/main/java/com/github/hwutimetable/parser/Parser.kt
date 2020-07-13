package com.github.hwutimetable.parser

import com.github.hwutimetable.parser.exceptions.ParserException
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*


/**
 * This class is used to parse the given html timetable document.
 * To get the timetable (days with no information) use [getTimetable].
 * To get the semester start day (for constructing [Timetable.TimetableInfo]
 * use [getSemesterStartDate].
 */
class Parser(private var document: Document?) : TimetableParser {
    private lateinit var timetableDays: Array<TimetableDay>

    init {
        initTimetableDays()
    }

    /**
     * Finds the rows of the day and returns the list of them
     * @param dayIndex: Index of the day of interest (0 - Monday, 1 - Tuesday etc.)
     * @return List of rows belonging to the day
     */
    private fun findRowsOfDay(daysTable: Document, dayIndex: Int): List<Element> {
        // Get all the children of tbody (trs) and remove the first row
        // because it contains time information that we don't need
        val rows = daysTable.selectFirst("tbody").children().drop(1)

        var currentDay = -1  // Current day index
        val dayRows = mutableListOf<Element>()  // List of rows that will be returned
        var skipCount = 0  // If > 0 then there are still some rows that belong to the current_day

        for (row in rows) {
            if (skipCount == 0)
                currentDay++
            else
                skipCount--

            val td = row.selectFirst("td.row-label-one")
            // If a td with this class does not exist, then this row belongs to the day only if current_day == day_index
            if (td == null) {
                if (currentDay == dayIndex)
                    dayRows.add(row)
                continue  // This row does not belong to the day, skip it
            }

            // The rowspan indicates how many rows are there per day
            val rowspan = td.attr("rowspan").toInt()
            if (rowspan == 1) {
                if (currentDay == dayIndex)
                    return listOf(row)  // There's only one row corresponding to that day - return list with the row
                else
                    continue  // Skip that row - it's not the day we are looking for
            }

            // There are more rows for the current_day
            if (currentDay == dayIndex)
                dayRows.add(row)

            skipCount = rowspan - 1  // To skip the day count
        }

        return dayRows
    }

    /**
     * Calculates the time by using the column (td) index.
     * @param colIndex: Column index
     */
    private fun getTime(colIndex: Int): LocalTime {
        var time = LocalTime(9, 15)
        // colIndex 0 is the day column, 1 corresponds to 9:15.
        colIndex.downTo(2).forEach { _ -> time = time.plusMinutes(15) }
        return time
    }

    /**
     * Parse the [td] with a table as a TimetableItem object
     * @param td: td with an item
     * @param tdCounter: td index at which the item appears
     * @return Colspan width of the item
     */
    private fun addLecture(td: Element, tdCounter: Int, dayIndex: Int): Int {
        val colspan = td.attr("colspan").toInt()  // Colspan tells us the duration of the lecture
        val startTime = getTime(tdCounter)
        val endTime = getTime(tdCounter + colspan)

        val tables = td.select("table")  // There are 3 tables for each item cell

        val orgInfo = tables[0]  // First table has the code, weeks and room
        val code = orgInfo.selectFirst("td[align=left]").text()
        val weeks = orgInfo.selectFirst("td[align=center]").text()
        val room = orgInfo.selectFirst("td[align=right]").text()

        // Currently, we are only interested in the item name
        val name = tables[1].selectFirst("td[align=center]").text()

        // The last table has the lecturer(s) name(s) and the type
        val lecInfo = tables[2]
        val lecturer = lecInfo.selectFirst("td[align=left]").text()
        val type = lecInfo.selectFirst("td[align=right]").text()

        timetableDays[dayIndex].items.add(
            TimetableItem(
                name = name,
                code = code,
                room = room,
                lecturer = lecturer,
                type = ItemType(type),
                start = startTime,
                end = endTime,
                weeks = WeeksBuilder()
                    .setFromString(weeks)
                    .getWeeks()
            )
        )

        return colspan
    }

    /**
     * With the given list of rows - finds all timetable items that are in them and adds them to the timetableItems list.
     * @param rows: List of rows
     */
    private fun addLecturesFromRows(rows: List<Element>, day: Int) {
        rows.forEachIndexed { index, row ->
            val columns = row.children()  // Children are the tds of the row
            var tdCounter = -1  // Current td

            columns.forEach { column ->
                tdCounter++

                if (tdCounter == 0) {
                    if (index == 0)  // Day info (Mon, Tue, Wed, ...)
                        return@forEach
                    else  // This is the nth row of the day (n > 0) and there's no day info for it
                        tdCounter++  // Count the "missing" td
                }

                if (column.hasClass("cell-border"))
                    return@forEach  // No lecture

                // Get the item and its width (colspan)
                val colspan = addLecture(column, tdCounter, day)
                tdCounter = tdCounter + colspan - 1  // There would be a double incrementation so takeaway 1
            }
        }
    }

    /**
     * Get start date of the semester
     */
    override fun getSemesterStartDate(): LocalDate {
        val datesHeader = document!!.selectFirst("span.header-2-2-3")
        val dateString = datesHeader.text()

        val regex = Regex("((\\d+\\s\\w+\\s\\d+)(?=-\\d+\\s\\w+\\s\\d+))")
        val dates = regex.find(dateString)
            ?: throw ParserException("Parser was not able to parse the start date of the semester ($dateString)")

        val startDate = dates.groups[0]
            ?: throw ParserException("Parser was not able to parse the start date of the semester ($dateString)")

        val formatter = DateTimeFormat.forPattern("dd MMM YYYY").withLocale(Locale.ENGLISH)
        return LocalDate.parse(startDate.value, formatter)
    }

    /**
     * Checks if the [document] has a parser
     * @return true if it does, false if it does not
     */
    private fun documentHasParser() = this.document!!.parser() != null

    override fun setDocument(document: Document): TimetableParser {
        this.document = document
        return this
    }

    private fun initTimetableDays() {
        this.timetableDays = arrayOf(
            TimetableDay(Day.MONDAY, arrayListOf()),
            TimetableDay(Day.TUESDAY, arrayListOf()),
            TimetableDay(Day.WEDNESDAY, arrayListOf()),
            TimetableDay(Day.THURSDAY, arrayListOf()),
            TimetableDay(Day.FRIDAY, arrayListOf())
        )
    }

    /**
     * Parses the timetable and return the timetable items
     * @return Timetable
     */
    @Throws(ParserException::class)
    override fun getTimetable(): Array<TimetableDay> {
        if (!documentHasParser())
            throw ParserException("Document must have a Jsoup parser")

        val daysTable = Jsoup.parse(document!!.selectFirst("table.grid-border-args").outerHtml())
        for (day in 0..4) {
            val rows = findRowsOfDay(daysTable, day)
            addLecturesFromRows(rows, day)
        }

        return timetableDays
    }
}
