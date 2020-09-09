package com.github.hwutimetable.parser

import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class CourseTimetableParser(
    var courseCode: String, var courseName: String,
    document: Document?, typeBackgroundProvider: TimetableClass.Type.BackgroundProvider
) : Parser(document, typeBackgroundProvider) {
    override fun getCode(classInfoTables: Elements): String {
        return courseCode
    }

    override fun getName(classInfoTables: Elements): String {
        return courseName
    }
}