package com.example.hwutimetable.parser

import org.jsoup.nodes.Document

interface TimetableParser {
    /**
     * Set the document for the parser.
     * To make the usage easier, "this" should be returned after setting the document.
     */
    fun setDocument(document: Document): TimetableParser

    /**
     * Get the timetable from the set document
     */
    fun getTimetable(): Timetable
}