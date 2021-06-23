package com.github.hwutimetable.updater

import com.github.hwutimetable.SampleTimetableHandler
import com.github.hwutimetable.filehandler.TimetableFileHandler
import com.github.hwutimetable.parser.Semester
import com.github.hwutimetable.parser.Timetable
import com.github.hwutimetable.parser.TimetableClass
import com.github.hwutimetable.scraper.CourseTimetableScraper
import com.github.hwutimetable.scraper.Option
import com.github.hwutimetable.scraper.ProgrammeTimetableScraper
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.setMain
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.jsoup.nodes.Document
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.times
import java.io.File

@DelicateCoroutinesApi
@ExperimentalCoroutinesApi
class UpdaterTest {
    private val updateWaitingTime = 500L
    private val sampleTimetablesDir = "src/test/resources/sampleTimetables"
    private val testDir = File(sampleTimetablesDir, "/parsed")
    private val backgroundCss = javaClass.classLoader!!.getResource("activitytype.css")
    private val typeBackgroundProvider = TimetableClass.Type.OnlineBackgroundProvider(backgroundCss)
    private val timetableHandler = SampleTimetableHandler(typeBackgroundProvider)

    init {
        // For coroutines
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @Before
    fun createParsedTimetable() {
        testDir.mkdir()
        val info = getTimetableInfo()
        val timetable = timetableHandler.getHtmlTimetable(File(sampleTimetablesDir, "test-timetable-org.html"), info)
        TimetableFileHandler(testDir).save(timetable)
    }

    private fun getTimetableInfo() =
        Timetable.Info("T0", "Test Timetable", Semester(LocalDate.now(), 1), LocalTime.parse("9:00"), false)

    @After
    fun replaceWithCopy() {
        testDir.listFiles { file ->
            file.delete()
        }
        testDir.delete()
    }

    @Test
    fun testReceiverAttached() {
        val receiver = Mockito.mock(NotificationReceiver::class.java)
        getUpdater(timetableHandler.getDocument(getOriginalTimetable())).apply {
            addInProgressListener(receiver)
            addFinishedListener(receiver)
            update()
        }

        // The updater will update in a separate thread. We have to wait for it to finish its work.
        Thread.sleep(updateWaitingTime)

        // If onUpdateFinished gets called then the receiver was successfully attached to the updater.
        Mockito.verify(receiver, times(1)).onUpdateFinished(emptySet())
    }

    @Test
    fun testUpdateStartedNotificationSent() {
        val receiver = Mockito.mock(NotificationReceiver::class.java)

        getUpdater(timetableHandler.getDocument(getOriginalTimetable())).apply {
            addInProgressListener(receiver)
            addFinishedListener(receiver)
            update()
        }

        Mockito.verify(receiver, times(1)).onUpdateInProgress()
    }

    @Test
    fun testNothingUpdated() {
        val file = getOriginalTimetable()
        val savedDocument = timetableHandler.getDocument(file)

        val receiver = Mockito.mock(NotificationReceiver::class.java)

        getUpdater(savedDocument).apply {
            addInProgressListener(receiver)
            addFinishedListener(receiver)
            update()
        }

        Thread.sleep(updateWaitingTime)
        Mockito.verify(receiver).onUpdateFinished(emptySet())
    }

    @Test
    fun testTimetableUpdated() {
        val newFile = File("src/test/resources/sampleTimetables/test-timetable-time-mod.html")
        val newTimetableDocument = timetableHandler.getDocument(newFile)

        val info = getTimetableInfo()
        val receiver = Mockito.mock(NotificationReceiver::class.java)

        getUpdater(newTimetableDocument).apply {
            addInProgressListener(receiver)
            addFinishedListener(receiver)
            update()
        }

        Thread.sleep(updateWaitingTime)
        Mockito.verify(receiver).onUpdateFinished(setOf(info))
    }

    private fun getOriginalTimetable() = File("src/test/resources/sampleTimetables/test-timetable-org.html")

    private fun getUpdater(scrapedTimetableDocument: Document): Updater {
        return Updater(
            testDir,
            TestProgrammeScraper(scrapedTimetableDocument),
            TestCourseScraper(scrapedTimetableDocument),
            typeBackgroundProvider
        )
    }

    /**
     * This class will be mocked by Mockito. We don't need to implement the methods
     * we will just check if they get invoked.
     */
    private class NotificationReceiver : OnUpdateInProgressListener, OnUpdateFinishedListener {
        override fun onUpdateInProgress() {
            return
        }

        override fun onUpdateFinished(updated: Collection<Timetable.Info>) {
            return
        }
    }

    private class TestProgrammeScraper(private val document: Document) : ProgrammeTimetableScraper {
        override suspend fun setup() {
            // Not required for testing
        }

        override fun getDepartments(): List<Option> {
            return emptyList()
        }

        override suspend fun getGroups(filters: Map<String, Any>): List<Option> {
            return emptyList()
        }

        override fun getLevels(): List<Option> {
            return emptyList()
        }

        override suspend fun getTimetable(filters: Map<String, Any>): Document {
            return document
        }
    }

    private class TestCourseScraper(private val document: Document) : CourseTimetableScraper {
        override suspend fun setup() {
            return
        }

        override fun getDepartments(): List<Option> {
            return emptyList()
        }

        override suspend fun getGroups(filters: Map<String, Any>): List<Option> {
            return emptyList()
        }

        override suspend fun getTimetable(filters: Map<String, Any>): Document {
            return document
        }
    }

}