package com.github.hwutimetable.filehandler

import com.github.hwutimetable.parser.*
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import java.io.File

class TimetableFileHandlerTest {
    companion object {
        private val dir_path: String = System.getProperty("user.dir") + "/tfh_test_dir"
        private val test_dir = File(dir_path)
        private lateinit var fileHandler: TimetableFileHandler

        @BeforeClass
        @JvmStatic
        fun setup() {
            if (test_dir.exists()) {
                test_dir.delete()
            }

            val success = test_dir.mkdir()
            if (!success) {
                fail("Unable to create timetable file handler test directory")
                return
            }
            fileHandler = TimetableFileHandler(test_dir)
        }

        @AfterClass
        @JvmStatic
        fun cleanUp() {
            if (!test_dir.exists())
                fail("The timetable file handler test directory does not exist before cleaning")
            test_dir.delete()
        }
    }

    @After
    fun deleteAllStoredTimetables() {
        val files = test_dir.listFiles()
        files?.forEach { file ->
            file.delete()
        }
    }

    @Test
    fun testSaveGetTimetable() {
        val timetable = generateTimetable("C01")

        fileHandler.save(timetable)
        val saved = fileHandler.getTimetable(timetable.info)
        assertEquals(timetable, saved)
    }

    @Test
    fun testGetStoredTimetables() {
        val timetables = listOf(
            generateTimetable("C01"),
            generateTimetable("C02"),
            generateTimetable("C03")
        )
        val infoList = timetables.map { it.info }

        timetables.forEach { timetable ->
            fileHandler.save(timetable)
        }

        val stored = fileHandler.getStoredTimetables()
        assertTrue(stored.containsAll(infoList))
    }

    @Test
    fun testDeleteTimetable() {
        val timetables = listOf(
            generateTimetable("C01"),
            generateTimetable("C02"),
            generateTimetable("C03")
        )
        val infoList = timetables.map { it.info }
        timetables.forEach { timetable ->
            fileHandler.save(timetable)
        }

        val deleted = infoList.find { it.code == "C01" }!!
        fileHandler.deleteTimetable(deleted)
        val expected = infoList.filter { it != deleted }

        val actual = fileHandler.getStoredTimetables()
        assertEquals(expected, actual)
    }

    private fun generateTimetable(code: String): Timetable {
        return Timetable(
            generateTimetableDays(),
            Timetable.TimetableInfo(
                code, "Test Timetable", Semester(
                    LocalDate.now(), 1
                )
            )
        )
    }

    private fun generateTimetableDays(): Array<TimetableDay> {
        return arrayOf(
            generateTimetableDay(Day.MONDAY, 1),
            generateTimetableDay(Day.TUESDAY, 2),
            generateTimetableDay(Day.WEDNESDAY, 3)
        )
    }

    private fun generateTimetableDay(day: Day, itemsInDay: Int): TimetableDay {
        val list = mutableListOf<TimetableItem>()
        for (i in 1..itemsInDay)
            list.add(generateTimetableItem())
        return TimetableDay(day, ArrayList(list))
    }

    private fun generateTimetableItem(): TimetableItem {
        return TimetableItem(
            "code",
            "name",
            "room",
            "lecturer",
            ItemType("type"),
            LocalTime.MIDNIGHT,
            LocalTime.MIDNIGHT,
            WeeksBuilder()
                .setRange(1, 1)
                .getWeeks()
        )
    }
}