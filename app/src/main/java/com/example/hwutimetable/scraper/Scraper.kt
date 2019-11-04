package com.example.hwutimetable.scraper

import org.jsoup.*
import org.jsoup.nodes.Document

class Scraper {
    private val loginUrl = "https://timetable.hw.ac.uk/WebTimetables/LiveED/login.aspx"
    private val defaultUrl = "https://timetable.hw.ac.uk/WebTimetables/LiveED/default.aspx"

    private var cookies: MutableMap<String, String> = mutableMapOf()
    private var response: Document? = null
    private var timetable: Document? = null

    /**
     * Searches for the input with the given [id] in the response document and returns its value
     * @param id input id
     * @return value as String, empty String if the input has no value
     */
    private fun getInputValue(id: String) = response?.selectFirst("#$id")?.`val`() ?: ""

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
     * Login in to the main (default) timetables website as a guest
     * @return status code after POST
     */
    fun login(): Int {
        var connection = Jsoup.connect(loginUrl).execute()
        cookies.putAll(connection.cookies())
        response = connection.parse()

        val formData = getRequiredFormData()
        formData["bGuestLogin"] = "Guest"

        connection = Jsoup.connect(loginUrl).data(formData).cookies(cookies).method(Connection.Method.POST).execute()
        response = connection.parse()
        cookies.putAll(connection.cookies())
        return connection.statusCode()
    }

    /**
     * Go to the Student Group Timetables (equivalent of clicking Student Groups link)
     * @return status code after transition
     */
    fun goToProgrammesTimetables(): Int {
        val formData = getRequiredFormData()
        formData["tLinkType"] = "information"
        formData["__EVENTARGUMENT"] = ""
        formData["__EVENTTARGET"] = "LinkBtn_studentsets"

        val connection = Jsoup.connect(defaultUrl)
            .data(formData)
            .cookies(cookies)
            .method(Connection.Method.POST)
            .execute()
        response = connection.parse()
        cookies.putAll(connection.cookies())
        return connection.statusCode()
    }

    /**
     * Get a list of the departments from the Student Group Timetables site
     * @return List of department options
     */
    fun getDepartments() = response?.selectFirst("#dlFilter2")
        ?.select("option")
        ?.map {
            Option(it.`val`(), it.text())
        }

    /**
     * Get a list of the levels from the Student Group Timetables site
     * @return List of level options
     */
    fun getLevels() = response?.selectFirst("#dlFilter")
        ?.select("option")
        ?.map {
            Option(it.`val`(), it.text())
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

        val data = getRequiredFormData()
        data.putAll(
            mapOf(
                "tLinkType" to "studentsets",
                "tWildcard" to "",
                "lbWeeks" to weeks,
                "lbDays" to "1-5",
                "dlPeriod" to "6-41",
                "dlType" to "individual;swsurl;SWSCUST Student Set Individual"
            )
        )
        return data
    }

    /**
     * Scrapes groups from the Student Group Timetable site
     * @param department department option's value (not text)
     * @param level level option's value (not text)
     * @return List of group options
     */
    fun getGroups(department: String, level: String): List<Option>? {
        // Define filter methods. Both of them return HTTP status code
        fun filterByDepartment(): Int {
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

            val connection = Jsoup.connect(defaultUrl)
                .data(formData)
                .cookies(cookies)
                .method(Connection.Method.POST)
                .execute()
            response = connection.parse()
            cookies.putAll(connection.cookies())
            return connection.statusCode()
        }

        fun filterByLevel(): Int {
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

            val connection = Jsoup.connect(defaultUrl)
                .data(formData)
                .cookies(cookies)
                .method(Connection.Method.POST)
                .execute()
            response = connection.parse()
            cookies.putAll(connection.cookies())
            return connection.statusCode()
        }

        filterByDepartment()
        filterByLevel()

        return response?.selectFirst("#dlObject")
            ?.select("option")
            ?.map {
                Option(it.`val`(), it.text())
            }
    }

    fun getTimetable(department: String, level: String, group: String, semester: Int): Document {
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

        val connection = Jsoup.connect(defaultUrl)
            .data(formData)
            .cookies(cookies)
            .method(Connection.Method.POST)
            .execute()
        return connection.parse()
    }
}