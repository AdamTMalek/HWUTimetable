package com.example.hwutimetable.updater

import com.example.hwutimetable.SampleTimetableHandler
import com.example.hwutimetable.filehandler.TimetableInfo
import com.example.hwutimetable.parser.Parser
import com.example.hwutimetable.parser.Timetable
import com.example.hwutimetable.parser.TimetableParser
import com.example.hwutimetable.scraper.Option
import com.example.hwutimetable.scraper.TimetableScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.setMain
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.After
import org.junit.Assert.fail
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.times
import java.io.File

@ExperimentalCoroutinesApi
class UpdaterTest {
    private val parser: ParserForTest
    private val testDir = File("src/test/resources/sampleTimetables", "/parsed")
    private val updateWaitingTime = 500L
    private val sampleTimetablePath = "src/test/resources/sampleTimetables/tt1.html"

    init {
        val savedTimetableFile = File("src/test/resources/sampleTimetables/parsed/#SPLUS4F80E0.json")
        val savedTimetable = SampleTimetableHandler.getTimetable(savedTimetableFile)
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
        val savedTimetable = SampleTimetableHandler.getTimetable(file)
        val savedDocument = SampleTimetableHandler.getDocument(file)

        if (savedDocument == null || savedTimetable == null) {
            fail("Loader failed to load from the given file. The path is not be valid.")
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
        val savedTimetable = SampleTimetableHandler.getTimetable(oldFile)
        val newTimetableDocument = SampleTimetableHandler.getDocument(newFile)

        if (savedTimetable == null || newTimetableDocument == null) {
            fail("Test resources are null. Check file paths.")
            return
        }

        val scraper = ScraperForTest(newTimetableDocument)
        val info = TimetableInfo("#SPLUS4F80E0", "BEng Computing and Electronics, level 3, semester 1", 1)
        val receiver = Mockito.mock(NotificationReceiver::class.java)
        val parser = Parser()
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

        override fun onUpdateFinished(updated: Collection<TimetableInfo>) {
            return
        }
    }

    private class ParserForTest(private val timetable: Timetable) : TimetableParser {
        override fun setDocument(document: Document): TimetableParser {
            return this
        }

        override fun getTimetable(): Timetable {
            return timetable
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