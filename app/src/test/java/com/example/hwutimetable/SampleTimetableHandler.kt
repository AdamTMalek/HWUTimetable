package com.example.hwutimetable

import com.example.hwutimetable.filehandler.InfoFile
import com.example.hwutimetable.filehandler.TimetableFileHandler
import com.example.hwutimetable.parser.Parser
import com.example.hwutimetable.parser.Timetable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File

object SampleTimetableHandler {
    fun getDocument(file: File): Document? {
        if (!file.exists())
            return null
        return Jsoup.parse(file, "UTF-8")
    }

    private fun getHtmlTimetable(file: File): Timetable? {
        val document = getDocument(file) ?: return null
        return Parser().setDocument(document).getTimetable()
    }

    private fun getJsonTimetable(file: File): Timetable? {
        val code = Regex("(#\\w+)").find(file.name)!!.groups.first()!!.value
        val info = InfoFile(file.parentFile!!).getInfoByCode(code) ?: return null
        return TimetableFileHandler(file.parentFile!!).getTimetable(info)
    }

    fun getTimetable(file: File): Timetable? {
        return if (file.extension == "json")
            getJsonTimetable(file)
        else
            getHtmlTimetable(file)

    }
}