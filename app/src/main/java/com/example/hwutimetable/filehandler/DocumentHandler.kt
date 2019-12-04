package com.example.hwutimetable.filehandler

import android.content.Context
import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.io.IOException

/**
 * This class provides methods that allow saving the JSoup documents as HTML files
 * and also keeps a record of any additional information for a timetable
 */
object DocumentHandler {
    /**
     * Saves the given [document] (timetable) as an HTML timetable.
     * @param context: Context of the application - used to get the files directory
     * @param document: JSoup document of the timetable
     * @param timetableInfo: Information of the timetable (code and name)
     */
    fun save(context: Context, document: Document, timetableInfo: TimetableInfo) {
        InfoFile.save(context, timetableInfo)
        saveTimetable(context, document, timetableInfo.code)
    }

    /**
     * Save the timetable as HTML document with [timetableName] as the name
     * (without the extension as it is added in the method)
     * @param context: Context of the application - used to get the files directory
     * @param document: Timetable to save
     * @param timetableName: Group code
     */
    private fun saveTimetable(
        context: Context,
        document: Document,
        timetableName: String
    ) {
        try {
            val file = File(context.filesDir, "$timetableName.html")
            file.writeText(document.outerHtml())
        } catch (exception: IOException) {
            Log.e("DocSaver", "IOException has occurred when writing to the file")
        }
    }

    /**
     * Gets the list of the timetables stored on the device
     */
    fun getStoredTimetables(context: Context): List<TimetableInfo> {
        return InfoFile.getList(context)
    }

    /**
     * Gets the JSoup document of a timetable
     */
    fun getTimetable(context: Context, title: String): Document {
        val infoList = InfoFile.getList(context)
        val filenameInfo = infoList.firstOrNull { it.name == title }
            ?: throw FileHandlerException(
                "Timetable \"$title\" does not exist",
                FileHandlerException.Reason.NOT_FOUND
            )

        val timetableFile = File(context.filesDir, getFilenameByInfo(filenameInfo))

        return Jsoup.parse(timetableFile, "UTF-8", title)
    }

    /**
     * Deletes a timetable from the device
     * @return true on success, false otherwise
     */
    fun deleteTimetable(context: Context, name: String): Boolean {
        val timetableInfo = InfoFile.getInfoByName(context, name)
            ?: throw FileHandlerException("Timetable not found in the info file", FileHandlerException.Reason.NOT_FOUND)

        var result = true
        try {
            InfoFile.delete(context, timetableInfo)
        } catch (ex: FileHandlerException) {
            result = false
        }
        return result
    }

    /**
     * Deletes all timetables stored on the device
     * @return List of the deleted timetables
     */
    fun deleteAllTimetables(context: Context): List<TimetableInfo> {
        val deleted = mutableListOf<TimetableInfo>()
        InfoFile.getList(context).forEach {
            if (deleteTimetable(context, it.name))
                deleted.add(it)
        }
        return deleted
    }

    /**
     * Adds the .html suffix to the timetable code taken from [info]
     */
    private fun getFilenameByInfo(info: TimetableInfo): String {
        return "${info.code}.html"
    }
}