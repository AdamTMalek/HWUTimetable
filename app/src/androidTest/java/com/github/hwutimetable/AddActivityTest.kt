package com.github.hwutimetable

import android.content.Context
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.github.hwutimetable.di.CurrentDateProviderModule
import com.github.hwutimetable.di.FileModule
import com.github.hwutimetable.di.TimetableScraperModule
import com.github.hwutimetable.filehandler.TimetableFileHandler
import com.github.hwutimetable.scraper.Option
import com.github.hwutimetable.scraper.TimetableScraper
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
import org.jsoup.nodes.Document
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@UninstallModules(value = [TimetableScraperModule::class, FileModule::class, CurrentDateProviderModule::class])
@HiltAndroidTest
class AddActivityTest {
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
    abstract class TimetableScraperTestModule {
        @Binds
        abstract fun bindScraper(scraperForTest: ScraperForTest): TimetableScraper
    }

    @Module
    @InstallIn(ApplicationComponent::class)
    abstract class TestDateProviderModule {
        @Singleton
        @Binds
        abstract fun bindDateProvider(testDate: TestDateProvider): CurrentDateProvider
    }

    class TestDateProvider @Inject constructor() : CurrentDateProvider {
        var date: LocalDate = LocalDate.now()
        override fun getCurrentDate(): LocalDate {
            return date
        }
    }

    @Singleton
    class ScraperForTest @Inject constructor() : TimetableScraper {
        override suspend fun setup() {
            // We don't need to do anything
        }

        override fun getDepartments(): List<Option> {
            return listOf(
                Option("dval0", "(Any Department)"),
                Option("dval1", "dep1"),
                Option("dval2", "dep2")
            )
        }

        override fun getLevels(): List<Option> {
            return listOf(
                Option("lval0", "(Any Level)"),
                Option("lval1", "lev1"),
                Option("lval2", "lev2")
            )
        }

        override suspend fun getGroups(department: String, level: String): List<Option> {
            return listOf(
                Option("gval1", "grp1 (Semester 1)"),
                Option("gval2", "grp2 (Semester 1)")
            )
        }

        override suspend fun getTimetable(group: String, semester: Int): Document {
            val context = InstrumentationRegistry.getInstrumentation().context
            val input = context.resources.openRawResource(com.github.hwutimetable.test.R.raw.tt1)
            return SampleTimetableHandler().getDocument(input)!!
        }
    }

    @Inject
    lateinit var fileHandler: TimetableFileHandler

    @Inject
    lateinit var testDate: CurrentDateProvider

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<AddActivity>

    @Before
    fun init() {
        hiltRule.inject()
    }

    private fun launchActivity() {
        scenario = ActivityScenario.launch(AddActivity::class.java)
    }

    private fun setDate(date: LocalDate) {
        (testDate as TestDateProvider).date = date
    }

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
            activity.findViewById<CheckBox>(R.id.follow_checkbox).isChecked = false
            activity.findViewById<Button>(R.id.submit_button).performClick()
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
            activity.findViewById<CheckBox>(R.id.follow_checkbox).isChecked = true
            activity.findViewById<Button>(R.id.submit_button).performClick()
        }

        assertNull(fileHandler.getStoredTimetables().find { it.name == name })
    }

    @Test
    fun testSemesterIsSetTo1() {
        setDate(LocalDate.parse("2020-09-01"))
        launchActivity()
        scenario.onActivity { activity ->
            val selectedSemester = activity.findViewById<Spinner>(R.id.semester_spinner).selectedItem
            assertEquals("Semester 1", selectedSemester)
        }
    }

    @Test
    fun testSemesterIsSetTo2() {
        setDate(LocalDate.parse("2020-01-01"))
        launchActivity()
        scenario.onActivity { activity ->
            val selectedSemester = activity.findViewById<Spinner>(R.id.semester_spinner).selectedItem
            assertEquals("Semester 2", selectedSemester)
        }
    }
}