package com.github.hwutimetable

import android.content.Context
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.github.hwutimetable.di.CurrentDateProviderModule
import com.github.hwutimetable.di.FileModule
import com.github.hwutimetable.di.ProgrammeScraperModule
import com.github.hwutimetable.filehandler.TimetableFileHandler
import com.github.hwutimetable.scraper.ProgrammeTimetableScraper
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import junit.framework.TestCase.*
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import javax.inject.Inject

@UninstallModules(value = [FileModule::class, ProgrammeScraperModule::class, CurrentDateProviderModule::class])
@HiltAndroidTest
class AddProgrammeActivityTest {
    @Module
    @InstallIn(ApplicationComponent::class)
    object TestTimetableFileHandlerModule {
        @Provides
        fun provideDirectory(@ApplicationContext context: Context): File {
            return File(context.filesDir, "/test/")
        }
    }

    @Module
    @InstallIn(ApplicationComponent::class)
    abstract class TestProgrammeScraper {
        @Binds
        abstract fun bindScraper(scraperForTest: TestScraper): ProgrammeTimetableScraper
    }

    @Module
    @InstallIn(ApplicationComponent::class)
    abstract class TestDateProviderModule {
        @Binds
        abstract fun bindDateProvider(testDate: TestDateProvider): CurrentDateProvider
    }

    @Inject
    lateinit var fileHandler: TimetableFileHandler

    @Inject
    lateinit var testDate: CurrentDateProvider

    @Inject
    lateinit var scraper: ProgrammeTimetableScraper

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<AddProgrammeTimetableActivity>
    private val semester1Date = LocalDate.parse("2020-09-01")
    private val semester2Date = LocalDate.parse("2020-01-01")

    @Before
    fun init() {
        hiltRule.inject()
    }

    private fun launchActivity() {
        scenario = ActivityScenario.launch(AddProgrammeTimetableActivity::class.java)
    }

    private fun setDate(date: LocalDate) {
        (testDate as TestDateProvider).setDate(date)
    }

    private fun getContext() = InstrumentationRegistry.getInstrumentation().targetContext

    @After
    fun cleanup() {
        fileHandler.deleteAllTimetables()
        scenario.close()
    }

    @Test
    fun testDepartmentsListGetsPopulated() {
        launchActivity()
        scenario.onActivity { activity ->
            val departmentsSpinner = activity.findViewById<Spinner>(R.id.departments_spinner)
            assertTrue(departmentsSpinner.adapter.count > 0)
        }
    }

    @Test
    fun testLevelsListGetsPopulated() {
        launchActivity()
        scenario.onActivity { activity ->
            val levelsSpinner = activity.findViewById<Spinner>(R.id.levels_spinner)
            assertTrue(levelsSpinner.adapter.count > 0)
        }
    }

    @Test
    fun testGroupsListGetsPopulated() {
        launchActivity()
        // To test if groups list get populated, we first have to select
        // a department and a level

        // Split the onActivity functions into two. First will apply the filters,
        // then the second will check if the groups spinner was populated.
        // If we join them together, it will not work, because groups
        // are fetched asynchronously.
        scenario.onActivity { activity ->
            activity.findViewById<Spinner>(R.id.departments_spinner).setSelection(1)
            activity.findViewById<Spinner>(R.id.levels_spinner).setSelection(1)
        }

        scenario.onActivity { activity ->
            val groupsSpinner = activity.findViewById<Spinner>(R.id.groups_spinner)
            assertTrue(groupsSpinner.count > 0)
        }
    }

    @Test
    fun testViewTimetableWithoutSaving() {
        launchActivity()
        var name = ""
        scenario.onActivity { activity ->
            activity.findViewById<Spinner>(R.id.departments_spinner).setSelection(1)
            activity.findViewById<Spinner>(R.id.levels_spinner).setSelection(1)
        }

        scenario.onActivity { activity ->
            val groupsSpinner = activity.findViewById<Spinner>(R.id.groups_spinner)
            groupsSpinner.setSelection(0)
            name = groupsSpinner.selectedItem as String
            activity.findViewById<CheckBox>(R.id.save_checkbox).isChecked = false
            activity.findViewById<Button>(R.id.get_timetable).performClick()
        }

        assertNull(fileHandler.getStoredTimetables().find { it.name == name })
    }

    @Test
    fun testSaveAndViewTimetable() {
        launchActivity()
        var name = ""
        scenario.onActivity { activity ->
            activity.findViewById<Spinner>(R.id.departments_spinner).setSelection(1)
            activity.findViewById<Spinner>(R.id.levels_spinner).setSelection(1)
        }

        scenario.onActivity { activity ->
            val groupsSpinner = activity.findViewById<Spinner>(R.id.groups_spinner)
            groupsSpinner.setSelection(0)
            name = groupsSpinner.selectedItem as String
            activity.findViewById<CheckBox>(R.id.save_checkbox).isChecked = true
            activity.findViewById<Button>(R.id.get_timetable).performClick()
        }

        assertNull(fileHandler.getStoredTimetables().find { it.name == name })
    }

    @Test
    fun testSemesterIsSetTo1() {
        setDate(semester1Date)
        launchActivity()
        scenario.onActivity { activity ->
            val selectedSemester = activity.findViewById<Spinner>(R.id.semester_spinner).selectedItem
            assertEquals("Semester 1", selectedSemester)
        }
    }

    @Test
    fun testSemesterIsSetTo2() {
        setDate(semester2Date)
        launchActivity()
        scenario.onActivity { activity ->
            val selectedSemester = activity.findViewById<Spinner>(R.id.semester_spinner).selectedItem
            assertEquals("Semester 2", selectedSemester)
        }
    }

    @Test
    fun testSemester1FilterWorks() {
        setDate(semester1Date)
        launchActivity()
        scenario.onActivity { activity ->
            activity.findViewById<Spinner>(R.id.departments_spinner).setSelection(1)
            activity.findViewById<Spinner>(R.id.levels_spinner).setSelection(1)
            // Semester spinner will set be to Semester 1 based on the date
        }

        scenario.onActivity { activity ->
            val spinnerAdapter = activity.findViewById<Spinner>(R.id.groups_spinner).adapter
            val groups = (0 until spinnerAdapter.count).map { spinnerAdapter.getItem(it) }
            val expected = listOf((scraper as TestScraper).semesterOneGroup)
            assertEquals(expected, groups)
        }
    }

    @Test
    fun testSemester2FilterWorks() {
        setDate(semester2Date)
        launchActivity()
        scenario.onActivity { activity ->
            activity.findViewById<Spinner>(R.id.departments_spinner).setSelection(1)
            activity.findViewById<Spinner>(R.id.levels_spinner).setSelection(1)
            // Semester spinner will set be to Semester 2 based on the date
        }

        scenario.onActivity { activity ->
            val spinnerAdapter = activity.findViewById<Spinner>(R.id.groups_spinner).adapter
            val groups = (0 until spinnerAdapter.count).map { spinnerAdapter.getItem(it) }
            val expected = listOf((scraper as TestScraper).semesterTwoGroup)
            assertEquals(expected, groups)
        }
    }

    @Test
    fun testAnySemesterFilterWorks() {
        // We don't care about the date for this one
        launchActivity()
        scenario.onActivity { activity ->
            activity.findViewById<Spinner>(R.id.departments_spinner).setSelection(1)
            activity.findViewById<Spinner>(R.id.levels_spinner).setSelection(1)
            activity.findViewById<Spinner>(R.id.semester_spinner).setSelection(2)  // Any semester
        }

        scenario.onActivity { activity ->
            val spinnerAdapter = activity.findViewById<Spinner>(R.id.groups_spinner).adapter
            val groups = (0 until spinnerAdapter.count).map { spinnerAdapter.getItem(it) }
            val testScraper = (scraper as TestScraper)
            val expected = listOf(testScraper.semesterOneGroup, testScraper.semesterTwoGroup)
            assertEquals(expected, groups)
        }
    }
}