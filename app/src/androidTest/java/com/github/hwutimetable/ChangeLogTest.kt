package com.github.hwutimetable

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.platform.app.InstrumentationRegistry
import com.github.hwutimetable.di.CourseScraperModule
import com.github.hwutimetable.di.FileModule
import com.github.hwutimetable.di.NetworkUtilitiesModule
import com.github.hwutimetable.di.ProgrammeScraperModule
import com.github.hwutimetable.extensions.getSharedPreferences
import com.github.hwutimetable.network.NetworkUtils
import com.github.hwutimetable.scraper.CourseTimetableScraper
import com.github.hwutimetable.scraper.ProgrammeTimetableScraper
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.DelicateCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton


@DelicateCoroutinesApi
@UninstallModules(
    value = [FileModule::class, NetworkUtilitiesModule::class, ProgrammeScraperModule::class,
        CourseScraperModule::class]
)
@HiltAndroidTest
class ChangeLogTest {
    @InstallIn(SingletonComponent::class)
    @Module
    object TestTimetableFileHandlerModule {
        @Provides
        fun provideDirectory(@ApplicationContext context: Context): File {
            return File(context.filesDir, "/test/")
        }
    }

    @InstallIn(SingletonComponent::class)
    @Module
    abstract class TestNetworkUtilitiesModule {
        class TestNetworkUtilities @Inject constructor() : NetworkUtils {

            var wifiOn = false
            var dataOn = false

            override fun hasInternetConnection(): Boolean {
                return wifiOn || dataOn
            }

            override fun isWifiEnabled(): Boolean {
                return wifiOn
            }

            override fun isMobileDataEnabled(): Boolean {
                return dataOn
            }
        }

        @Singleton
        @Binds
        abstract fun bindNetworkUtilities(networkUtilities: TestNetworkUtilities): NetworkUtils
    }

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class TestProgrammeScraper {
        @Binds
        abstract fun bindScraper(scraperForTest: TestScraper): ProgrammeTimetableScraper
    }

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class TestCourseScraper {
        @Binds
        abstract fun bindScraper(scraperForTest: com.github.hwutimetable.TestCourseScraper): CourseTimetableScraper
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>

    private val context = InstrumentationRegistry.getInstrumentation().context
    private val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

    private val lastVersionKey = targetContext.getString(R.string.last_ran_version)
    private val sharedPrefs = targetContext.getSharedPreferences(R.string.shared_pref_file_key, Context.MODE_PRIVATE)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @After
    fun cleanup() {
        if (this::scenario.isInitialized)
            scenario.close()
    }

    private fun launchActivity() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun testChangeLogIsShownAfterUpdate() {
        with(sharedPrefs.edit()) {
            putBoolean(targetContext.getString(R.string.first_run), false)
            remove(lastVersionKey)
            apply()
        }
        launchActivity()
        onView(withSubstring(targetContext.getString(R.string.changelog_dialog_title)))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testChangeLogNotShownIfNotAfterUpdate() {
        with(sharedPrefs.edit()) {
            putInt(lastVersionKey, BuildConfig.VERSION_CODE)
            apply()
        }

        launchActivity()
        onView(withSubstring(targetContext.getString(R.string.changelog_dialog_title)))
            .check(doesNotExist())
    }
}