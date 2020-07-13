package com.github.hwutimetable.parser

import org.joda.time.LocalDate
import org.jsoup.nodes.Document

interface TimetableParser {
    /**
     * Set the document for the parser.
     * To make the usage easier, "this" is returned after setting the document.
     */
    fun setDocument(document: Document): TimetableParser

    /**
     * Get the semester start date from the set document
     */
    fun getSemesterStartDate(): LocalDate

    /**
     * Get the timetable days from the set document
     */
    fun getTimetable(): Array<TimetableDay>
}