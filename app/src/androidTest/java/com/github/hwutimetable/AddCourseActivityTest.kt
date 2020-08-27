package com.github.hwutimetable

import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Spinner
import androidx.test.core.app.ActivityScenario
import com.github.hwutimetable.di.CourseScraperModule
import com.github.hwutimetable.scraper.CourseTimetableScraper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(value = [CourseScraperModule::class])
class AddCourseActivityTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<AddCourseActivity>

    @Module
    @InstallIn(ApplicationComponent::class)
    abstract class TestScraperModule {
        @Binds
        abstract fun bindScraper(scraper: TestCourseScraper): CourseTimetableScraper
    }

    private fun launchActivity(): ActivityScenario<AddCourseActivity> {
        scenario = ActivityScenario.launch(AddCourseActivity::class.java)
        return scenario
    }

    @Inject
    lateinit var scraper: CourseTimetableScraper

    @Before
    fun setup() {
        hiltRule.inject()
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
}
