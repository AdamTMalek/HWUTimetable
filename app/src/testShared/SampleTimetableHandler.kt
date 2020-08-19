package com.github.hwutimetable

import com.github.hwutimetable.filehandler.TimetableFileHandler
import com.github.hwutimetable.parser.Parser
import com.github.hwutimetable.parser.Timetable
import com.github.hwutimetable.parser.TimetableClass
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.io.InputStream
import java.net.URL

class SampleTimetableHandler {
    private val backgroundCss = URL("file:///android_res/raw/activitytype.css")
    private val typeBackgroundProvider = TimetableClass.Type.OnlineBackgroundProvider(backgroundCss)

    fun getDocument(file: File): Document? {
        if (!file.exists())
            return null
        return Jsoup.parse(file, "UTF-8")
    }

    fun getDocument(stream: InputStream): Document? {
        return Jsoup.parse(stream, "UTF-8", "")
    }

    fun getHtmlTimetable(file: File, info: Timetable.Info): Timetable {
        val document = getDocument(file)!!
        val parser = Parser(document, typeBackgroundProvider)
        val days = parser.getTimetable()

        return Timetable(days, info)
    }

    fun getJsonTimetable(file: File): Timetable? {
        val gson = TimetableFileHandler.getGson()
        return gson.fromJson(file.readText(), Timetable::class.java)
    }
}
