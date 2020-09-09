package com.github.hwutimetable.parser

import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class ProgrammeTimetableParser(document: Document?, typeBackgroundProvider: TimetableClass.Type.BackgroundProvider) :
    Parser(document, typeBackgroundProvider) {
    override fun getCode(classInfoTables: Elements): String {
        return classInfoTables[0].selectFirst("td[align=left]").text()
    }

    override fun getName(classInfoTables: Elements): String {
        return classInfoTables[1].selectFirst("td[align=center]").text()
    }

}