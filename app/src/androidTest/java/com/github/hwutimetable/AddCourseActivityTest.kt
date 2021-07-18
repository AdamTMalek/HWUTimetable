package com.github.hwutimetable

import android.content.Context
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Spinner
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.github.hwutimetable.di.CourseScraperModule
import com.github.hwutimetable.di.FileModule
import com.github.hwutimetable.filehandler.TimetableFileHandler
import com.github.hwutimetable.scraper.CourseTimetableScraper
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import junit.framework.TestCase.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import javax.inject.Inject

@DelicateCoroutinesApi
@HiltAndroidTest
@UninstallModules(value = [CourseScraperModule::class, FileModule::class])
class AddCourseActivityTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<AddCourseActivity>

    @Inject
    lateinit var scraper: CourseTimetableScraper

    @Inject
    lateinit var timetableFileHandler: TimetableFileHandler

    private val infoListPopulator by lazy {
        InfoListPopulator(timetableFileHandler)
    }

    @Before
    fun setup() {
        hiltRule.inject()
    }

    private fun launchActivity(): ActivityScenario<AddCourseActivity> {
        scenario = ActivityScenario.launch(AddCourseActivity::class.java)
        return scenario
    }

    @Test
    fun testTitleIsSet() {
        launchActivity().onActivity { activity ->
            val expectedTitle = activity.getString(R.string.add_course_activity_title)
            assertEquals(expectedTitle, activity.title)
        }
    }

    @Test
    fun testDepartmentsGetPopulated() {
        launchActivity().onActivity { activity ->
            val expectedDepartments = scraper.getDepartments().map { it.text }
            val departmentsSpinner = activity.findViewById<Spinner>(R.id.departments_spinner)
            val spinnerItems = (0 until departmentsSpinner.adapter.count).map { index ->
                departmentsSpinner.adapter.getItem(index)
            }
            assertEquals(expectedDepartments, spinnerItems)
        }
    }

    @Test
    fun testGroupsGetPopulated() {
        // Set semester filter to any
        launchActivity().onActivity { activity ->
            activity.findViewById<Spinner>(R.id.semester_spinner).setSelection(2, false)
        }

        scenario.onActivity { activity ->
            runBlocking {
                val expectedDepartments = scraper.getGroups(emptyMap()).map { it.text }
                val groupsInput = activity.findViewById<AutoCompleteTextView>(R.id.groups_input)
                val spinnerItems = (0 until groupsInput.adapter.count).map { index ->
                    groupsInput.adapter.getItem(index)
                }
                assertEquals(expectedDepartments, spinnerItems)
            }
        }
    }

    @Test
    fun testSemestersGetPopulated() {
        launchActivity().onActivity { activity ->
            val expectedSemester = activity.resources.getStringArray(R.array.semester_filter_values).toList()
            val semesterSpinner = activity.findViewById<Spinner>(R.id.semester_spinner)
            val actualSemesters = (0 until semesterSpinner.adapter.count).map { index ->
                semesterSpinner.adapter.getItem(index)
            }

            assertEquals(expectedSemester, actualSemesters)
        }
    }

    @Test
    fun testSemesterFilterWorks() {
        fun getGroups(groupsInput: AutoCompleteTextView) = (0 until groupsInput.adapter.count).map { index ->
            groupsInput.adapter.getItem(index).toString()
        }

        var expectedGroups: List<String>
        var actualGroups: List<String>

        launchActivity().onActivity { activity ->
            activity.findViewById<Spinner>(R.id.semester_spinner).setSelection(2, false)
        }

        scenario.onActivity { activity ->
            // Semester any
            runBlocking {
                expectedGroups = scraper.getGroups(emptyMap()).map { it.text }
                actualGroups = getGroups(activity.findViewById(R.id.groups_input))

                assertEquals(expectedGroups, actualGroups)

                // Semester 1
                activity.findViewById<Spinner>(R.id.semester_spinner).setSelection(0, false)
            }
        }

        scenario.onActivity { activity ->
            expectedGroups = listOf("C00AA-S1")
            actualGroups = getGroups(activity.findViewById(R.id.groups_input))

            assertEquals(expectedGroups, actualGroups)

            // Semester 2
            activity.findViewById<Spinner>(R.id.semester_spinner).setSelection(1, false)
        }

        scenario.onActivity { activity ->
            expectedGroups = listOf("C00AA-S2")
            actualGroups = getGroups(activity.findViewById(R.id.groups_input))

            assertEquals(expectedGroups, actualGroups)
        }
    }

    @Test
    fun testAddCourseIsDisabledByDefault() {
        launchActivity().onActivity { activity ->
            val isEnabled = activity.findViewById<Button>(R.id.add_course_button).isEnabled
            assertFalse(isEnabled)
        }
    }

    @Test
    fun testGenerateTimetableIsDisabledByDefault() {
        launchActivity().onActivity { activity ->
            val isEnabled = activity.findViewById<Button>(R.id.get_timetable).isEnabled
            assertFalse(isEnabled)
        }
    }

    @Test
    fun testGenerateButtonEnabledWhenNameUnique() {
        infoListPopulator.populateInfoList()
        launchActivity()
        selectCourse()

        onView(withId(R.id.timetable_name))
            .perform(typeText("1st year"), closeSoftKeyboard())

        scenario.onActivity {
            assertTrue(it.findViewById<Button>(R.id.get_timetable).isEnabled)
        }
    }

    @Test
    fun testGenerateButtonDisabledWhenNameTaken() {
        infoListPopulator.populateInfoList()
        launchActivity()
        selectCourse()

        onView(withId(R.id.timetable_name))
            .perform(typeText("Timetable 1"), closeSoftKeyboard())

        scenario.onActivity {
            assertFalse(it.findViewById<Button>(R.id.get_timetable).isEnabled)
        }
    }

    private fun selectCourse() {
        scenario.onActivity { activity ->
            activity.findViewById<Spinner>(R.id.departments_spinner).setSelection(1, false)
            activity.findViewById<Spinner>(R.id.semester_spinner).setSelection(2, false)
        }

        val groupText = runBlocking {
            return@runBlocking scraper.getGroups(emptyMap()).first().text
        }

        onView(withId(R.id.groups_input))
            .perform(click())
            .perform(typeText(groupText), closeSoftKeyboard())
            .inRoot(RootMatchers.isPlatformPopup())
            .perform(click(), closeSoftKeyboard())

        onView(withId(R.id.add_course_button))
            .perform(click())
    }

    @Before
    fun clearInfoFile() {
        timetableFileHandler.deleteAllTimetables()
    }

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class TestScraperModule {
        @Binds
        abstract fun bindScraper(scraper: TestCourseScraper): CourseTimetableScraper
    }

    @InstallIn(SingletonComponent::class)
    @Module
    object TestTimetableFileHandlerModule {
        @Provides
        fun provideDirectory(@ApplicationContext context: Context): File {
            return File(context.filesDir, "/test/")
        }
    }
}
