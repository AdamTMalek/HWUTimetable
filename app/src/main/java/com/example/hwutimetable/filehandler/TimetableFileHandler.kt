package com.example.hwutimetable.filehandler

import com.example.hwutimetable.parser.Timetable
import com.fatboyindustrial.gsonjodatime.LocalDateConverter
import com.fatboyindustrial.gsonjodatime.LocalTimeConverter
import com.fatboyindustrial.gsonjodatime.PeriodConverter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.io.IOException
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.Period
import java.io.File
import java.io.FileNotFoundException
import java.io.FilenameFilter
import javax.inject.Inject

class TimetableFileHandler @Inject constructor(private val directory: File) {
    companion object {
        fun getGson(): Gson {
            return GsonBuilder()
                .registerTypeAdapter(LocalDate::class.java, LocalDateConverter())
                .registerTypeAdapter(LocalTime::class.java, LocalTimeConverter())
                .registerTypeAdapter(Period::class.java, PeriodConverter())
                .create()
        }
    }

    private val filterRegex = Regex("[\\#a-zA-Z0-9]+\\.json")
    private val filenameFilter = FilenameFilter { _, name -> filterRegex.matches(name) }


    init {
        if (!directory.exists())
            directory.mkdir()
    }

    /**
     * Saves the given timetable and its info
     */
    @Throws(IOException::class)
    fun save(timetable: Timetable) {
        val file = File(directory, getFilename(timetable.info))

        if (!file.exists())
            file.createNewFile()

        val gson = getGson()
        val data = gson.toJson(timetable)

        file.writeText(data)
    }

    /**
     * Returns a list of stored timetable infos
     */
    fun getStoredTimetables(): List<Timetable.TimetableInfo> {
        return directory.listFiles(filenameFilter)?.map { file ->
            val timetable = getGson().fromJson(file.readText(), Timetable::class.java)
            return@map timetable.info
        } ?: return emptyList()
    }

    /**
     * Gets the timetable using its info
     * @throws FileNotFoundException when the timetable does not exist
     */
    @Throws(FileNotFoundException::class)
    fun getTimetable(timetableInfo: Timetable.TimetableInfo): Timetable {
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
    fun deleteTimetable(info: Timetable.TimetableInfo) {
        val file = File(directory, getFilename(info))
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
    fun deleteAllTimetables(): List<Timetable.TimetableInfo> {
        val deleted = mutableListOf<Timetable.TimetableInfo>()
        getStoredTimetables().forEach { info ->
            deleteTimetable(info)
            deleted.add(info)
        }
        return deleted
    }

    private fun getFilename(timetableInfo: Timetable.TimetableInfo) = "${timetableInfo.code}.json"

    private fun getNotFoundException(file: File): FileNotFoundException {
        return FileNotFoundException("Timetable file (${file.name}) was not found")
    }
}