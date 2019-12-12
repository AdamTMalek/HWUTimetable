package com.example.hwutimetable.filehandler

import com.example.hwutimetable.parser.Timetable
import com.fatboyindustrial.gsonjodatime.LocalTimeConverter
import com.fatboyindustrial.gsonjodatime.PeriodConverter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.io.IOException
import org.joda.time.LocalTime
import org.joda.time.Period
import java.io.File
import java.io.FileNotFoundException

class TimetableFileHandler(private val directory: File) : TimetableHandler {
    private val infoFile = InfoFile(directory)

    @Throws(IOException::class)
    /**
     * Saves the given timetable and its info
     */
    override fun save(timetable: Timetable, timetableInfo: TimetableInfo) {
        infoFile.save(timetableInfo)

        val file = File(directory, getFilename(timetableInfo))
        val gson = getGson()
        val data = gson.toJson(timetable)

        file.writeText(data)
    }

    /**
     * Returns a list of stored timetable infos
     */
    override fun getStoredTimetables(): List<TimetableInfo> {
        return infoFile.getList()
    }

    /**
     * Gets the timetable using its info
     * @throws FileNotFoundException when the timetable does not exist
     */
    @Throws(FileNotFoundException::class)
    override fun getTimetable(timetableInfo: TimetableInfo): Timetable {
        val file = File(directory, getFilename(timetableInfo))
        if (!file.exists())
            throw getNotFoundException(file)

        return getGson().fromJson(file.readText(), Timetable::class.java)
    }

    /**
     * Deletes the timetable using its info
     * @throws FileNotFoundException when the timetable does not exist
     */
    @Throws(FileNotFoundException::class)
    override fun deleteTimetable(timetableInfo: TimetableInfo) {
        infoFile.delete(timetableInfo)
        val file = File(directory, getFilename(timetableInfo))
        if (!file.exists())
            throw getNotFoundException(file)

        file.delete()
    }

    /**
     * Deletes all timetables stored on the device
     * @return List of TimetableInfo of the timetables that were successfully deleted
     * @throws FileNotFoundException when a timetable does not exist
     */
    @Throws(FileNotFoundException::class)
    override fun deleteAllTimetables(): List<TimetableInfo> {
        val deleted = mutableListOf<TimetableInfo>()
        infoFile.getList().forEach {
            deleteTimetable(it)
            deleted.add(it)
        }
        return deleted
    }

    private fun getGson(): Gson {
        return GsonBuilder().registerTypeAdapter(
            LocalTime::class.java, LocalTimeConverter()
        ).registerTypeAdapter(
          Period::class.java, PeriodConverter()
        ).create()
    }

    private fun getFilename(timetableInfo: TimetableInfo) = "${timetableInfo.code}.json"

    private fun getNotFoundException(file: File): FileNotFoundException {
        return FileNotFoundException("Timetable file (${file.name}) was not found")
    }
}