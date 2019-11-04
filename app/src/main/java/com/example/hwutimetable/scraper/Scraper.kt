package com.example.hwutimetable.scraper

import org.jsoup.*
import org.jsoup.nodes.Document

class Scraper {
    private val loginUrl = "https://timetable.hw.ac.uk/WebTimetables/LiveED/login.aspx"
    private val defaultUrl = "https://timetable.hw.ac.uk/WebTimetables/LiveED/default.aspx"

    private var response: Document? = null
    private var cookies : MutableMap<String, String> = mutableMapOf()

    /**
     * Searches for the input with the given [id] in the response document and returns its value
     * @param id input id
     * @return value as String, empty String if the input has no value
     */
    private fun getInputValue(id : String) = response?.selectFirst("#$id")?.`val`() ?: ""

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
    fun login() : Int {
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
    fun goToProgrammesTimetables() : Int {
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
     * Get a map of the departments from the Student Group Timetables site
     * @return Map where a key is option's value and value is option's text
     */
    fun getDepartments() : Map<String, String> {
        val options = response?.selectFirst("#dlFilter2")
            ?.select("option")
        val departments : MutableMap<String, String> =  mutableMapOf()
        options?.forEach { option -> departments[option.`val`()] = option.text()}
        return HashMap<String, String>(departments)
    }

    /**
     * Get a map of the levels from the Student Group Timetables site
     * @return Map where a key is option's value and value is option's text
     */
    fun getLevels() : Map<String, String> {
        val options = response?.selectFirst("#dlFilter")
            ?.select("option")
        val levels : MutableMap<String, String> = mutableMapOf()
        options?.forEach { option -> levels[option.`val`()] = option.text()}
        return HashMap<String, String>(levels)
    }

    /**
     * The Student Group Timetables POST requests require some additional form data
     * that stay the same in every request. This method returns the data we need
     * along with the 3 fields from [getRequiredFormData]
     * @return Mutable map with the required data
     */
    private fun getTimetableFormData() : MutableMap<String, String> {
        val data = getRequiredFormData()
        data.putAll(mapOf(
            "tLinkType" to "studentsets",
            "tWildcard" to "",
            "lbWeeks" to "1;2;3;4;5;6;7;8;9;10;11;12",
            "lbDays" to "1-5",
            "dlPeriod" to "6-41",
            "dlType" to "individual;swsurl;SWSCUST Student Set Individual"
        ))
        return data
    }

    /**
     * Scrapes groups from the Student Group Timetable site
     * @param department department option's value (not text)
     * @param level level option's value (not text)
     * @return Map where a key is the option's value and value is the option's text
     */
    fun getGroups(department : String, level : String) : Map<String, String> {
        // Define filter methods. Both of them return HTTP status code
        fun filterByDepartment() : Int {
            val formData = getTimetableFormData().apply {
                putAll(mapOf(
                    "__EVENTARGUMENT" to "",
                    "__EVENTTARGET" to "dlFilter2",
                    "dlFilter2" to department,
                    "dlFilter" to ""
                ))
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

        fun filterByLevel() : Int {
            val formData = getTimetableFormData().apply {
                putAll(mapOf(
                    "__EVENTARGUMENT" to "",
                    "__EVENTTARGET" to "dlFilter",
                    "dlFilter2" to department,
                    "dlFilter" to level
                ))
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

        // We can now construct the map
        val groups : MutableMap<String, String> = mutableMapOf()
        val options = response?.selectFirst("#dlObject")
            ?.select("option")
        options?.forEach{ option -> groups[option.`val`()] = option.text()}
        return HashMap<String, String>(groups)
    }
}

/**
 * The following main function is used for development purposes only
 * TODO: Delete the function below after finishing writing the module
 */
fun main() {
    val scraper = Scraper()
    println(scraper.login())
    println(scraper.goToProgrammesTimetables())
    val departments = scraper.getDepartments()
    departments.forEach {
        println("${it.key}: ${it.value}")
    }
    scraper.getLevels().forEach {
        println("${it.key}: ${it.value}")
    }

    val department = "FC60807364F1D08D810E00FDA8C9D9FE"
    val level = "Undergraduate Level 3"
    scraper.getGroups(department, level).forEach {
        println("${it.key}: ${it.value}")
    }
//    println(scraper.getTitle())
//    println(scraper.getRequiredFormData())
}