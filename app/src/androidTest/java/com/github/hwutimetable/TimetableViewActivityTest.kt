package com.github.hwutimetable

import android.app.Activity
import android.content.Intent
import android.widget.Spinner
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
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
import org.junit.*
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidTest
@UninstallModules(CurrentDateProviderModule::class)
class TimetableViewActivityTest {
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
        Timetable(days, Timetable.Info("C01", "N01", Semester(parser.getSemesterStartDate(), 1)))
    }

    private val aiLectureCode = "F29AI-S1"  // First lecture on Friday, weeks 2-11
    private val seLectureCode = "F29SO-S1"  // First lecture on Friday on week 1

    private lateinit var scenario: ActivityScenario<TimetableViewActivity>

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var dateProvider: CurrentDateProvider

    companion object {
        @BeforeClass
        @JvmStatic
        fun setOriginalViewPreference() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            with(sharedPreferences.edit()) {
                putBoolean(context.getString(R.string.use_simplified_view), false)
                apply()
            }
        }
    }

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
        val intent = Intent(targetContext, TimetableViewActivity::class.java)
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

    @Test
    fun testSelectedWeekChange() {
        fun getFirstLectureCode(activity: Activity): CharSequence {
            val grid = activity.findViewById<TimetableGridLayout>(R.id.timetable_grid)
            val firstLecture = grid.getTimetableItems().first()
            return firstLecture.findViewById<TextView>(R.id.class_code).text
        }
        /* To check if the selected week actually makes a difference
         * (i.e. displayed timetable differs)
         * we will set the date to the first week of the semester.
         * This way, we will NOT have the AI lecture at Friday 9:15,
         * then we will select the second week and check if that
         * lecture will "appear"
         */
        setDate(LocalDate.parse("2019-09-20"))  // Friday, week 1
        startActivity()

        scenario.onActivity { activity ->
            // Verify that the AI lecture is not showing
            val firstWeekFirstLectureCode = getFirstLectureCode(activity)
            assertEquals(seLectureCode, firstWeekFirstLectureCode)

            // Change to week 2, where the AI lecture should appear
            changeWeek(activity, 2)
        }

        // Split into two onActivity calls, otherwise checking and UI happen asynchronously
        // resulting in the test failing, even though the lecture pops up

        scenario.onActivity { activity ->
            // Verify that the AI lecture has showed up
            val secondWeekFirstLectureCode = getFirstLectureCode(activity)
            assertEquals(aiLectureCode, secondWeekFirstLectureCode)
        }
    }

    @Test
    fun testClashesMessageShowsUp() {
        setDate(LocalDate.parse("2019-09-16"))  // Monday, week 1
        startActivity()

        scenario.onActivity { activity ->
            changeWeek(activity, 3)
        }

        Espresso.onView(withText("Clashes")).check(matches(isDisplayed()))
    }

    private fun changeWeek(activity: Activity, week: Int) {
        val weeksSpinner = activity.findViewById<Spinner>(R.id.weeks_spinner)
        weeksSpinner.setSelection(week - 1, false)
    }

    @Test
    fun testPopupWindowOpensWhenClickedOnClass() {
        setDate(LocalDate.parse("2019-09-17"))
        startActivity()
        Espresso.onView(
            allOf(
                withText("Signals and Systems"),
                hasSibling(withText("GR1DLb"))
            )
        ).perform(longClick())
        Espresso.onView(withId(R.id.item_info_grid)).check(matches(isDisplayed()))
    }

    @Test
    fun testPopupWindowHasCorrectItem() {
        setDate(LocalDate.parse("2019-09-17"))
        startActivity()
        Espresso.onView(
            allOf(
                withText("Signals and Systems"),
                hasSibling(withText("GR1DLb"))
            )
        ).perform(longClick())

        Espresso.onView(withId(R.id.item_info_grid))
            .check(matches(withChild(withText("Signals and Systems"))))
    }

    @Test
    fun testPopupWindowCloses() {
        setDate(LocalDate.parse("2019-09-17"))
        startActivity()
        Espresso.onView(
            allOf(
                withText("Signals and Systems"),
                hasSibling(withText("GR1DLb"))
            )
        ).perform(longClick())
        Espresso.onView(withId(R.id.close_class_info)).perform(click())
        Espresso.onView(withId(R.id.item_info_grid)).check(doesNotExist())
    }
}