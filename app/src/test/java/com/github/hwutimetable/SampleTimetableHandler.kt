package com.github.hwutimetable

import com.github.hwutimetable.filehandler.TimetableFileHandler
import com.github.hwutimetable.parser.Parser
import com.github.hwutimetable.parser.Timetable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File

object SampleTimetableHandler {
    fun getDocument(file: File): Document? {
        if (!file.exists())
            return null
        return Jsoup.parse(file, "UTF-8")
    }

    fun getHtmlTimetable(file: File, info: Timetable.TimetableInfo): Timetable {
        val document = getDocument(file)!!
        val parser = Parser(document)
        val days = parser.getTimetable()

        return Timetable(days, info)
    }

    fun getJsonTimetable(file: File): Timetable? {
        val gson = TimetableFileHandler.getGson()
        return gson.fromJson(file.readText(), Timetable::class.java)
    }
}