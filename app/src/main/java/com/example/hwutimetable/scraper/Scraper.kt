package com.example.hwutimetable.scraper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * The [Scraper] class is the main implementation of [TimetableScraper]
 * that is responsible for getting through the forms on the timetable
 * website and getting the timetable itself.
 *
 * The network functions are suspending and run on a separate networking
 * thread by using Kotlin's Dispatcher.IO.
 */
class Scraper : TimetableScraper {
    private val loginUrl = "https://timetable.hw.ac.uk/WebTimetables/LiveED/login.aspx"
    private val defaultUrl = "https://timetable.hw.ac.uk/WebTimetables/LiveED/default.aspx"

    private var state = ScraperState.OnLoginSite
    private var cookies: MutableMap<String, String> = mutableMapOf()
    private var response: Document? = null

    private var department = ""
    private var level = ""

    /**
     * Searches for the input with the given [id] in the response document and returns its value
     * @param id input id
     * @return value as String
     */
    private fun getInputValue(id: String): String {
        checkNotNull(response) { "Response cannot be null" }
        return response!!.selectFirst("#$id")?.`val`() ?: throw ScraperException("No element with id #$id")
    }

    /**
     * Every form data must have __VIEWSTATE __VIEWSTATEGENERATOR and __EVENTVALIDATION fields
     * This method returns a map with these 3 fields
     * @return MutableMap with __VIEWSTATE __VIEWSTATEGENERATOR and __EVENTVALIDATION
     */
    private fun getRequiredFormData() = mutableMapOf(
        "__VIEWSTATE" to getInputValue("__VIEWSTATE"),
        "__VIEWSTATEGENERATOR" to getInputValue("__VIEWSTATEGENERATOR"),
        "__EVENTVALIDATION" to getInputValue("__EVENTVALIDATION")
    )

    /**
     * Uses JSoup and performs a GET request to the given [url] without any cookies
     * or any other data.
     * After the request is executed, cookies are saved to the [cookies] variable and
     * the [response] is set.
     *
     * @return Response from the server
     * @throws HttpStatusException when response code is not HTTP 200 (OK)
     */
    @Throws(HttpStatusException::class)
    private suspend fun getSite(url: String) = withContext(Dispatchers.IO) {
        Jsoup.connect(url)
            .ignoreHttpErrors(true)
            .execute()
            .also {
                cookies.putAll(it.cookies())
                response = it.parse()

                if (it.statusCode() != 200)
                    throw HttpStatusException("HTTP error fetching URL", it.statusCode(), it.url().toString())
            }
    }

    /**
     * Uses JSoup and performs a POST request to the given [url] with existing [cookies]
     * and with the given [formData].
     * After the request is executed, [cookies] and [response] are updated.
     *
     * @return Response from the server
     * @throws HttpStatusException when response code is not HTTP 200 (OK)
     */
    @Throws(HttpStatusException::class)
    private suspend fun submitForm(url: String, formData: Map<String, String>) = withContext(Dispatchers.IO) {
        Jsoup.connect(url)
            .ignoreHttpErrors(true)
            .data(formData)
            .cookies(cookies)
            .method(Connection.Method.POST)
            .execute()
            .also {
                cookies.putAll(it.cookies())
                response = it.parse()

                if (it.statusCode() != 200)
                    throw HttpStatusException("HTTP error fetching URL", it.statusCode(), it.url().toString())
            }
    }

    /**
     * Login in to the main (default) timetables website as a guest
     * @return status code after POST
     */
    private suspend fun login(): Int {
        getSite(loginUrl)

        val formData = getRequiredFormData()
        formData["bGuestLogin"] = "Guest"

        val connection = submitForm(loginUrl, formData)
        state = ScraperState.LoggedIn

        return connection.statusCode()
    }

    /**
     * Go to the Student Group Timetables (equivalent of clicking Student Groups link)
     * @return status code after transition
     */
    private suspend fun goToProgrammesTimetables(): Int {
        check(state == ScraperState.LoggedIn || state == ScraperState.Finished) {
            "Cannot perform this action when not logged in"
        }

        val formData = getRequiredFormData().apply {
            putAll(
                mapOf(
                    "tLinkType" to "information",
                    "__EVENTARGUMENT" to "",
                    "__EVENTTARGET" to "LinkBtn_studentsets"
                )
            )
        }

        val connection = submitForm(defaultUrl, formData)
        state = ScraperState.OnTimetablesSite

        return connection.statusCode()
    }

    /**
     * Performs login and goes to programmes timetables (Student Groups)
     */
    override suspend fun setup() {
        login()
        goToProgrammesTimetables()
    }

    /**
     * Get a list of the departments from the Student Group Timetables site
     * @return List of department options
     */
    @Throws(ScraperException::class)
    override fun getDepartments(): List<Option> {
        check(state == ScraperState.OnTimetablesSite) {
            "Departments can only be gotten from the programmes timetables site"
        }

        return response!!.selectFirst("#dlFilter2")
            ?.select("option")
            ?.map {
                Option(it.`val`(), it.text())
            } ?: throw ScraperException("Could not find department options")
    }

    /**
     * Get a list of the levels from the Student Group Timetables site
     * @return List of level options
     */
    @Throws(ScraperException::class)
    override fun getLevels(): List<Option> {
        check(state == ScraperState.OnTimetablesSite) {
            "Levels can only be gotten from the programmes timetables site"
        }

        return response!!.selectFirst("#dlFilter")
            ?.select("option")
            ?.map {
                Option(it.`val`(), it.text())
            } ?: throw ScraperException("Could not find level options")
    }

    /**
     * The Student Group Timetables POST requests require some additional form data
     * that stay the same in every request. This method returns the data we need
     * along with the 3 fields from [getRequiredFormData]
     * @return Mutable map with the required data
     */
    private fun getTimetableFormData(semester: Int = 1): MutableMap<String, String> {
        val weeks = when (semester) {
            1 -> "1;2;3;4;5;6;7;8;9;10;11;12"
            2 -> "18;19;20;21;22;23;24;25;26;27;28;29"
            else -> throw IllegalArgumentException("semester must be either 1 or 2")
        }

        return getRequiredFormData().apply {
            putAll(
                mapOf(
                    "tLinkType" to "studentsets",
                    "tWildcard" to "",
                    "lbWeeks" to weeks,
                    "lbDays" to "1-5",
                    "dlPeriod" to "6-41",
                    "dlType" to "individual;swsurl;SWSCUST Student Set Individual"
                )
            )
        }
    }

    /**
     * Filter by department and then by level
     * @param department department option's value (not text)
     * @param level level option's value (not text)
     * @return HTTP status code
     */
    private suspend fun filter(department: String, level: String): Int {
        suspend fun filterByDepartment(department: String): Int {
            val formData = getTimetableFormData().apply {
                putAll(
                    mapOf(
                        "__EVENTARGUMENT" to "",
                        "__EVENTTARGET" to "dlFilter2",
                        "dlFilter2" to department,
                        "dlFilter" to ""
                    )
                )
            }

            return submitForm(defaultUrl, formData).statusCode()
        }

        check(state == ScraperState.OnTimetablesSite) {
            "Cannot apply filters unless on timetables site"
        }

        filterByDepartment(department)
        val formData = getTimetableFormData().apply {
            putAll(
                mapOf(
                    "__EVENTARGUMENT" to "",
                    "__EVENTTARGET" to "dlFilter",
                    "dlFilter2" to department,
                    "dlFilter" to level
                )
            )
        }

        val responseCode = submitForm(defaultUrl, formData).statusCode()
        state = ScraperState.Filtered
        return responseCode
    }

    /**
     * Scrapes groups from the Student Group Timetable site
     * @param department department option's value (not text)
     * @param level level option's value (not text)
     * @return List of group options
     */
    override suspend fun getGroups(department: String, level: String): List<Option> {
        check(state == ScraperState.OnTimetablesSite || state == ScraperState.Filtered) {
            "Illegal state for getGroups. Must be OnTimetablesSite or Filter."
        }

        // Apply the filter
        filter(department, level)
        state = ScraperState.Filtered
        this.department = department
        this.level = level

        return response!!.selectFirst("#dlObject")
            ?.select("option")
            ?.map {
                Option(it.`val`(), it.text())
            } ?: throw ScraperException("Could not find group options")
    }

    /**
     * Get the timetable document from the website with the given [group] id (option value)
     * and the semester.
     * @param group group Option value
     * @param semester Integer 1 or 2
     * @return HTML document with the timetable
     */
    override suspend fun getTimetable(group: String, semester: Int): Document {
        if (state == ScraperState.Finished) {
            cookies.clear()
            response = null
            login()
            goToProgrammesTimetables()
        }

        // We have to apply filters even though we know the group option value
        // Otherwise we will get an error "No items have been selected for your request"
        if (state != ScraperState.Filtered) {
            filter("", "")
            return getTimetable(group, semester)
        }

        val formData = getTimetableFormData(semester).apply {
            putAll(
                mapOf(
                    "__EVENTARGUMENT" to "",
                    "__EVENTTARGET" to "",
                    "dlFilter2" to department,
                    "dlFilter" to level,
                    "dlObject" to group,
                    "bGetTimetable" to "View Timetable"
                )
            )
        }

        submitForm(defaultUrl, formData)
        state = ScraperState.Finished

        response = Jsoup.parse(response!!.outerHtml())
        return response as Document
    }

    /**
     * ScraperState represents different state that the Scraper can be in
     */
    private enum class ScraperState {
        OnLoginSite,
        LoggedIn,
        OnTimetablesSite,
        Filtered, // Department and/or level filter has been applied
        Finished  // Finished scraping
    }
}