package com.github.hwutimetable.filehandler

import com.fatboyindustrial.gsonjodatime.LocalDateConverter
import com.fatboyindustrial.gsonjodatime.LocalTimeConverter
import com.fatboyindustrial.gsonjodatime.PeriodConverter
import com.github.hwutimetable.parser.Timetable
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.Period
import java.io.File
import java.io.FileNotFoundException
import java.io.FilenameFilter
import java.io.IOException
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

    private val orderFile = File(directory, "timetables.txt")
    private val filterRegex = Regex("[\\#a-zA-Z0-9]+\\.json")
    private val filenameFilter = FilenameFilter { _, name -> filterRegex.matches(name) }


    init {
        if (!directory.exists())
            directory.mkdir()
    }

    /**
     * Saves the given timetable.
     * Gets placed last in the orders file.
     */
    @Throws(IOException::class)
    fun save(timetable: Timetable) {
        val file = File(directory, getFilename(timetable.info))

        if (!file.exists())
            file.createNewFile()

        val gson = getGson()
        val data = gson.toJson(timetable)

        file.writeText(data)

        val newCode = timetable.info.code
        val order = getTimetablesOrder().toMutableList()

        if (order.contains(newCode))
            return

        order.add(timetable.info.code)
        orderFile.writeText(order.joinToString(","))
    }

    fun saveOrder(infoList: List<Timetable.Info>) {
        val order = infoList.joinToString(",") { it.code }
        orderFile.writeText(order)
    }

    private fun getTimetablesOrder(): List<String> {
        if (!orderFile.exists())
            return emptyList()

        return orderFile.readText().split(",")
    }

    /**
     * Returns a list of stored timetable infos sorted by user's preference
     */
    fun getStoredTimetables(): List<Timetable.Info> {
        if (!orderFile.exists())
            return emptyList()

        val order = getTimetablesOrder().withIndex().associate { it.value to it.index }

        return directory.listFiles(filenameFilter)?.map { file ->
            val timetable = getGson().fromJson(file.readText(), Timetable::class.java)
            return@map timetable.info
        }?.sortedBy { order[it.code] } ?: return emptyList()
    }

    /**
     * Gets the timetable using its info
     * @throws FileNotFoundException when the timetable does not exist
     */
    @Throws(FileNotFoundException::class)
    fun getTimetable(info: Timetable.Info): Timetable {
        val file = File(directory, getFilename(info))
        if (!file.exists())
            throw getNotFoundException(file)

        return getGson().fromJson(file.readText(), Timetable::class.java)
    }

    /**
     * Updates the name of a stored timetable.
     */
    fun updateName(updatedInfo: Timetable.Info) {
        val timetable = getTimetable(updatedInfo)
        timetable.info.name = updatedInfo.name
        save(timetable)
    }

    /**
     * Deletes the timetable using its info
     * @throws FileNotFoundException when the timetable does not exist
     */
    @Throws(FileNotFoundException::class)
    fun deleteTimetable(info: Timetable.Info) {
        val file = File(directory, getFilename(info))
        if (!file.exists())
            throw getNotFoundException(file)

        file.delete()

        val newOrder = getTimetablesOrder()
            .filterNot { it == info.code }
            .joinToString(",")

        orderFile.writeText(newOrder)
    }

    /**
     * Deletes all timetables stored on the device
     * @return List of Info of the timetables that were successfully deleted
     * @throws FileNotFoundException when a timetable does not exist
     */
    @Throws(FileNotFoundException::class)
    fun deleteAllTimetables(): List<Timetable.Info> {
        val deleted = mutableListOf<Timetable.Info>()
        getStoredTimetables().forEach { info ->
            deleteTimetable(info)
            deleted.add(info)
        }

        orderFile.delete()
        return deleted
    }

    private fun getFilename(info: Timetable.Info) = "${info.code}.json"

    private fun getNotFoundException(file: File): FileNotFoundException {
        return FileNotFoundException("Timetable file (${file.name}) was not found")
    }
}