package com.github.hwutimetable

import android.content.Intent
import android.widget.Spinner
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.github.hwutimetable.di.CurrentDateProviderModule
import com.github.hwutimetable.parser.Parser
import com.github.hwutimetable.parser.Semester
import com.github.hwutimetable.parser.Timetable
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import junit.framework.TestCase.assertEquals
import kotlinx.android.synthetic.main.activity_main.*
import org.hamcrest.Matchers.allOf
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidTest
@UninstallModules(CurrentDateProviderModule::class)
class ViewTimetableActivityTest {
    @Module
    @InstallIn(ApplicationComponent::class)
    abstract class TestDateProviderModule {
        @Singleton
        @Binds
        abstract fun bindDateProvider(testDate: TestDateProvider): CurrentDateProvider
    }

    class TestDateProvider @Inject constructor() : CurrentDateProvider {
        lateinit var date: LocalDate
        override fun getCurrentDate(): LocalDate {
            return date
        }
    }

    private val context = InstrumentationRegistry.getInstrumentation().context
    private val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val timetable: Timetable by lazy {
        val input = context.resources.openRawResource(com.github.hwutimetable.test.R.raw.tt1)
        val document = SampleTimetableHandler().getDocument(input)!!
        val parser = Parser(document)
        val days = parser.getTimetable()
        Timetable(days, Timetable.TimetableInfo("C01", "N01", Semester(parser.getSemesterStartDate(), 1)))
    }

    private lateinit var scenario: ActivityScenario<ViewTimetableActivity>

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var dateProvider: CurrentDateProvider

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun cleanup() {
        if (this::scenario.isInitialized)
            scenario.close()
    }

    private fun startActivity() {
        val intent = Intent(targetContext, ViewTimetableActivity::class.java)
        intent.putExtra("timetable", timetable)
        scenario = ActivityScenario.launch(intent)
    }

    private fun setDate(date: LocalDate = LocalDate.now()) {
        (dateProvider as TestDateProvider).date = date
    }

    @Test
    fun testTitleIsSet() {
        setDate()
        startActivity()
        scenario.onActivity { activity ->
            val actualTitle = activity.toolbar.title
            assertEquals(timetable.info.name, actualTitle)
        }
    }

    @Test
    fun testCurrentDayIsSetWhenWeekday() {
        val weekDays = mapOf(
            "20" to "Monday",
            "21" to "Tuesday",
            "22" to "Wednesday",
            "23" to "Thursday",
            "24" to "Friday"
        )

        weekDays.forEach { (date, day) ->
            setDate(LocalDate.parse("2020-07-$date"))
            startActivity()
            scenario.onActivity { activity ->
                val dayLabel = activity.findViewById<TextView>(R.id.section_label)
                assertEquals(day, dayLabel.text)
            }
            scenario.close()
        }
    }

    @Test
    fun testPreviousDayIsSet() {
        val weekDays = mapOf(
            "20" to "",
            "21" to "Monday",
            "22" to "Tuesday",
            "23" to "Wednesday",
            "24" to "Thursday"
        )

        weekDays.forEach { (date, day) ->
            setDate(LocalDate.parse("2020-07-$date"))
            startActivity()
            scenario.onActivity { activity ->
                val dayLabel = activity.findViewById<TextView>(R.id.previous_day_label)
                assertEquals(day, dayLabel.text)
            }
            scenario.close()
        }
    }

    @Test
    fun testNextDayIsSet() {
        val weekDays = mapOf(
            "20" to "Tuesday",
            "21" to "Wednesday",
            "22" to "Thursday",
            "23" to "Friday"
        )

        weekDays.forEach { (date, day) ->
            setDate(LocalDate.parse("2020-07-$date"))
            startActivity()
            scenario.onActivity { activity ->
                val dayLabel = activity.findViewById<TextView>(R.id.next_day_label)
                assertEquals(day, dayLabel.text)
            }
            scenario.close()
        }
    }

    @Test
    fun testDayIsSetToMondayWhenWeekend() {
        val weekendDays = listOf("25", "26")
        weekendDays.forEach { date ->
            setDate(LocalDate.parse("2020-07-$date"))
            startActivity()
            scenario.onActivity { activity ->
                val dayLabel = activity.findViewById<TextView>(R.id.section_label)
                assertEquals("Monday", dayLabel.text)
            }
            scenario.close()
        }
    }

    @Test
    fun testPreviousDayLabelClick() {
        setDate(LocalDate.parse("2020-07-22"))  // Wednesday
        startActivity()
        Espresso.onView(allOf(isDisplayed(), withId(R.id.previous_day_label))).perform(click())
        Espresso.onView(allOf(isDisplayed(), withId(R.id.section_label))).check(
            matches(withText("Tuesday"))
        )
    }

    @Test
    fun testNextDayLabelClick() {
        setDate(LocalDate.parse("2020-07-22"))  // Wednesday
        startActivity()
        Espresso.onView(allOf(isDisplayed(), withId(R.id.next_day_label))).perform(click())
        Espresso.onView(allOf(isDisplayed(), withId(R.id.section_label))).check(
            matches(withText("Thursday"))
        )
    }

    @Test
    fun testCurrentWeekIsSetAccordingToDate() {
        // To ensure that the selected week works as it should, we will set the date to the second week
        // of the timetable that we use for testing. This is 23/09/2019.
        setDate(LocalDate.parse("2019-09-23"))
        startActivity()
        scenario.onActivity { activity ->
            val weeksSpinner = activity.findViewById<Spinner>(R.id.weeks_spinner)
            val selectedWeek = weeksSpinner.selectedItem
            assertEquals(2, selectedWeek)
        }
    }

    @Test
    fun testWeeksIsSetTo12AfterEndOfSemester() {
        // We will set a date that is past the end of the semester. This can be anything
        setDate(LocalDate.parse("2020-07-23"))
        startActivity()
        scenario.onActivity { activity ->
            val weeksSpinner = activity.findViewById<Spinner>(R.id.weeks_spinner)
            val selectedWeek = weeksSpinner.selectedItem
            assertEquals(12, selectedWeek)
        }
    }
}