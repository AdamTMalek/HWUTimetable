package com.example.hwutimetable.updater

import com.example.hwutimetable.SampleTimetableHandler
import com.example.hwutimetable.parser.*
import com.example.hwutimetable.scraper.Option
import com.example.hwutimetable.scraper.TimetableScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.setMain
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.After
import org.junit.Assert.fail
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.times
import java.io.File
import java.util.*

@ExperimentalCoroutinesApi
class UpdaterTest {
    private val parser: ParserForTest
    private val testDir = File("src/test/resources/sampleTimetables", "/parsed")
    private val updateWaitingTime = 500L
    private val sampleTimetablePath = "src/test/resources/sampleTimetables/tt1.html"
    private val localDateFormatter = DateTimeFormat.forPattern("dd/MM/YYYY").withLocale(Locale.ENGLISH)
    private val timetableInfo = Timetable.TimetableInfo(
        "#SPLUS4F80E0", "BEng Computing and Electronics, level 3, semester 1",
        Semester(LocalDate.parse("16/09/2019", localDateFormatter), 1)
    )

    init {
        val savedTimetableFile = File("src/test/resources/sampleTimetables/parsed/#SPLUS4F80E0.json")
        val savedTimetable = SampleTimetableHandler.getJsonTimetable(savedTimetableFile)
        parser = ParserForTest(savedTimetable as Timetable)

        // For coroutines
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @After
    fun replaceWithCopy() {
        val original = File("src/test/resources/sampleTimetables/parsed/#SPLUS4F80E0.json")
        val copy = File("src/test/resources/sampleTimetables/parsed/#SPLUS4F80E0-COPY.json")

        original.writeText(copy.readText())
    }

    @Test
    fun testReceiverAttached() {
        val scraper = ScraperForTest(Jsoup.parse(sampleTimetablePath))
        val receiver = Mockito.mock(NotificationReceiver::class.java)
        Updater(testDir, parser, scraper).apply {
            addInProgressListener(receiver)
            addFinishedListener(receiver)
            update()
        }

        // The updater will update in a separate thread. We have to wait for it to finish its work.
        Thread.sleep(updateWaitingTime)

        // If onUpdateFinished gets called then the receiver was successfully attached to the updater.
        Mockito.verify(receiver, times(1)).onUpdateFinished(emptyList())
    }

    @Test
    fun testUpdateStartedNotificationSent() {
        val scraper = ScraperForTest(Jsoup.parse(sampleTimetablePath))
        val receiver = Mockito.mock(NotificationReceiver::class.java)
        Updater(testDir, parser, scraper).apply {
            addInProgressListener(receiver)
            addFinishedListener(receiver)
            update()
        }

        Mockito.verify(receiver, times(1)).onUpdateInProgress()
    }

    @Test
    fun testNothingUpdated() {
        val file = File("src/test/resources/sampleTimetables/tt1.html")
        val savedDocument = SampleTimetableHandler.getDocument(file)

        if (savedDocument == null) {
            fail("Loader failed to load from the given file. The path is not valid.")
            return
        }

        val scraper = ScraperForTest(savedDocument)
        val receiver = Mockito.mock(NotificationReceiver::class.java)
        Updater(testDir, parser, scraper).apply {
            addInProgressListener(receiver)
            addFinishedListener(receiver)
            update()
        }

        Thread.sleep(updateWaitingTime)
        Mockito.verify(receiver).onUpdateFinished(emptyList())
    }

    @Test
    fun testTimetableUpdated() {
        val oldFile = File("src/test/resources/sampleTimetables/tt1.html")
        val newFile = File("src/test/resources/sampleTimetables/tt2.html")
        val oldFileDoc = SampleTimetableHandler.getDocument(oldFile)!!
        val newTimetableDocument = SampleTimetableHandler.getDocument(newFile)

        if (newTimetableDocument == null) {
            fail("Test resources are null. Check file paths.")
            return
        }

        val scraper = ScraperForTest(newTimetableDocument)
        val semesterStartDate = Parser(oldFileDoc).getSemesterStartDate()
        val info = Timetable.TimetableInfo(
            "#SPLUS4F80E0", "BEng Computing and Electronics, level 3, semester 1", Semester(
                semesterStartDate, 1
            )
        )
        val receiver = Mockito.mock(NotificationReceiver::class.java)
        val parser = Parser(null)
        Updater(testDir, parser, scraper).apply {
            addInProgressListener(receiver)
            addFinishedListener(receiver)
            update()
        }

        Thread.sleep(updateWaitingTime)
        Mockito.verify(receiver).onUpdateFinished(listOf(info))
    }

    /**
     * This class will be mocked by Mockito. We don't need to implement the methods
     * we will just check if they get invoked.
     */
    private class NotificationReceiver : OnUpdateInProgressListener, OnUpdateFinishedListener {
        override fun onUpdateInProgress() {
            return
        }

        override fun onUpdateFinished(updated: Collection<Timetable.TimetableInfo>) {
            return
        }
    }

    private class ParserForTest(private val timetable: Timetable) : TimetableParser {
        override fun setDocument(document: Document): TimetableParser {
            return this
        }

        override fun getSemesterStartDate(): LocalDate {
            return timetable.info.semester.startDate
        }

        override fun getTimetable(): Array<TimetableDay> {
            return timetable.days
        }
    }

    private class ScraperForTest(private val document: Document) : TimetableScraper {
        override suspend fun setup() {
            // Not required for testing
        }

        override fun getDepartments(): List<Option> {
            return emptyList()
        }

        override fun getLevels(): List<Option> {
            return emptyList()
        }

        override suspend fun getGroups(department: String, level: String): List<Option> {
            return emptyList()
        }

        override suspend fun getTimetable(group: String, semester: Int): Document {
            return document
        }
    }
}