package com.github.hwutimetable

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.github.hwutimetable.di.CurrentDateProviderModule
import com.github.hwutimetable.di.FileModule
import com.github.hwutimetable.di.ProgrammeScraperModule
import com.github.hwutimetable.filehandler.TimetableFileHandler
import com.github.hwutimetable.parser.ProgrammeTimetableParser
import com.github.hwutimetable.parser.Semester
import com.github.hwutimetable.parser.Timetable
import com.github.hwutimetable.parser.TimetableClass
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
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidTest
@UninstallModules(value = [CurrentDateProviderModule::class, ProgrammeScraperModule::class, FileModule::class])
class TimetableViewActivityTest {
    private val backgroundProvider = object : TimetableClass.Type.BackgroundProvider {
        override suspend fun getBackgroundColor(type: String) = "#FFFFFF"
    }

    @InstallIn(ApplicationComponent::class)
    @Module
    object TestTimetableFileHandlerModule {
        @Provides
        fun provideDirectory(@ApplicationContext context: Context): File {
            return File(context.filesDir, "/test/")
        }
    }

    @Module
    @InstallIn(ApplicationComponent::class)
    abstract class TestDateProviderModule {
        @Singleton
        @Binds
        abstract fun bindDateProvider(testDate: TestDateProvider): CurrentDateProvider
    }

    @Module
    @InstallIn(ApplicationComponent::class)
    abstract class TestProgrammeScraper {
        @Binds
        abstract fun bindScraper(scraperForTest: TestScraper): ProgrammeTimetableScraper
    }

    private val context = InstrumentationRegistry.getInstrumentation().context
    private val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val timetable: Timetable by lazy {
        getTimetable()
    }

    private val aiLectureCode = "F29AI-S1"  // First lecture on Friday, weeks 2-11
    private val seLectureCode = "F29SO-S1"  // First lecture on Friday on week 1

    private lateinit var scenario: ActivityScenario<TimetableViewActivity>

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var fileHandler: TimetableFileHandler

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
        startActivity(timetable)
    }

    private fun startActivity(timetable: Timetable) {
        val intent = Intent(targetContext, TimetableViewActivity::class.java)
        intent.putExtra("timetable", timetable)
        scenario = ActivityScenario.launch(intent)
    }

    private fun setDate(date: LocalDate = LocalDate.now()) {
        (dateProvider as TestDateProvider).setDate(date)
    }

    @Test
    fun testTitleIsSet() {
        setDate()
        startActivity()
        scenario.onActivity { activity ->
            val actualTitle = activity.findViewById<Toolbar>(R.id.toolbar).title
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

    @Test
    fun testOldTimetableStartTime() {
        val startTime = LocalTime.parse("9:00")
        val timetable = getTimetable(startTime)
        startActivity(timetable)

        Espresso.onView(thatMatchesFirst(withText("9:00"))).check(matches(isDisplayed()))
    }

    @Test
    fun testNewTimetableStartTime() {
        val startTime = LocalTime.parse("9:15")
        val timetable = getTimetable(startTime)
        startActivity(timetable)

        Espresso.onView(thatMatchesFirst(withText("9:15"))).check(matches(isDisplayed()))
    }

    @Test
    fun testRenameOpensDialog() {
        startActivity()
        Espresso.openActionBarOverflowOrOptionsMenu(context)
        Espresso.onView(withText("Rename")).perform(click())

        Espresso.onView(withText(targetContext.getString(R.string.rename_dialog_title))).check(matches(isDisplayed()))
    }

    @Test
    fun testRenamingWorks() {
        startActivity()
        Espresso.openActionBarOverflowOrOptionsMenu(context)
        Espresso.onView(withText("Rename")).perform(click())
        Espresso.onView(withId(R.id.edit_timetable_title)).perform(clearText(), typeText("Renamed timetable"))
        Espresso.onView(allOf(withText("Rename"), instanceOf(Button::class.java))).perform(click())

        assertNotNull(fileHandler.getStoredTimetables().find { it.name == "Renamed timetable" })
    }

    private fun getTimetable(startTime: LocalTime = LocalTime.parse("9:00")): Timetable {
        val input = context.resources.openRawResource(com.github.hwutimetable.test.R.raw.tt1)
        val document = SampleTimetableHandler(backgroundProvider).getDocument(input)!!
        val parser = ProgrammeTimetableParser(document, backgroundProvider)
        val days = parser.getTimetable()
        return Timetable(
            days,
            Timetable.Info("C01", "N01", Semester(parser.getSemesterStartDate(), 1), startTime, false)
        )
    }
}