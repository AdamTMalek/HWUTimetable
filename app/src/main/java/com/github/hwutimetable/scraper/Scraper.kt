package com.github.hwutimetable.scraper

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
abstract class Scraper : TimetableScraper {
    private val loginUrl = "https://timetable.hw.ac.uk/WebTimetables/LiveED/login.aspx"
    protected val defaultUrl = "https://timetable.hw.ac.uk/WebTimetables/LiveED/default.aspx"

    protected var state = ScraperState.OnLoginSite
    protected var cookies: MutableMap<String, String> = mutableMapOf()
    protected var response: Document? = null

    protected var department = ""
    protected var level = ""

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
    protected fun getRequiredFormData() = mutableMapOf(
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
    protected suspend fun getSite(url: String): Connection.Response = withContext(Dispatchers.IO) {
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
    protected suspend fun submitForm(url: String, formData: Map<String, String>) = withContext(Dispatchers.IO) {
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
    protected suspend fun login(): Int {
        getSite(loginUrl)

        val formData = getRequiredFormData()
        formData["bGuestLogin"] = "Guest"

        val connection = submitForm(loginUrl, formData)
        state = ScraperState.LoggedIn

        return connection.statusCode()
    }

    /**
     * Performs login and goes to programmes timetables (Student Groups)
     */
    override suspend fun setup() {
        login()
        goToProgrammesTimetables()
    }

    protected abstract suspend fun goToProgrammesTimetables(): Int

    /**
     * Get a list of the departments from the Student Group Timetables site
     * @return List of department options
     */
    @Throws(ScraperException::class)
    override fun getDepartments(): List<Option> {
        check(state == ScraperState.OnTimetablesSite) {
            "Departments can only be gotten from the programmes timetables site"
        }

        return mapSelectionToOptions("#dlFilter2")
            ?: throw ScraperException("Could not find department options")
    }

    /**
     * The Student Group Timetables POST requests require some additional form data
     * that stay the same in every request. This method returns the data we need
     * along with the 3 fields from [getRequiredFormData]
     * @return Mutable map with the required data
     */
    protected fun getTimetableFormData(semester: Int = 1): MutableMap<String, String> {
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
                    "dlPeriod" to "5-40",
                    "dlType" to "individual;swsurl;SWSCUST Student Set Individual"
                )
            )
        }
    }

    protected suspend fun filterByDepartment(department: String): Int {
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

    protected fun mapSelectionToOptions(id: String): List<Option>? {
        return response!!.selectFirst(id)
            ?.select("option")
            ?.map { Option(it.`val`(), it.text()) }
    }

    protected fun canApplyFilters() = state == ScraperState.OnTimetablesSite || state == ScraperState.Filtered

    /**
     * ScraperState represents different state that the Scraper can be in
     */
    protected enum class ScraperState {
        OnLoginSite,
        LoggedIn,
        OnTimetablesSite,
        Filtered, // Department and/or level filter has been applied
        Finished  // Finished scraping
    }

    class FilterBuilder {
        private val filters = mutableMapOf<String, Any>()

        fun withDepartment(department: String): FilterBuilder {
            filters["department"] = department
            return this
        }

        fun withLevel(department: String): FilterBuilder {
            filters["level"]
            return this
        }

        fun withSemester(semester: Int): FilterBuilder {
            filters["semester"] = semester
            return this
        }

        fun withGroup(group: String): FilterBuilder {
            filters["group"] = group
            return this
        }

        fun getFilter(): Map<String, Any> = filters
    }
}