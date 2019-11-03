package com.example.hwutimetable.scraper

import org.jsoup.*
import org.jsoup.nodes.Document

class Scraper {
    private val login_url = "https://timetable.hw.ac.uk/WebTimetables/LiveED/login.aspx"
    private val default_url = "https://timetable.hw.ac.uk/WebTimetables/LiveED/default.aspx"

    private var response: Document? = null
    private var timetable: Document? = null
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
        var connection = Jsoup.connect(login_url).execute()
        cookies.putAll(connection.cookies())
        response = connection.parse()

        val formData = getRequiredFormData()
        formData["bGuestLogin"] = "Guest"

        connection = Jsoup.connect(login_url).data(formData).cookies(cookies).method(Connection.Method.POST).execute()
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

        val connection = Jsoup.connect(default_url)
            .data(formData)
            .cookies(cookies)
            .method(Connection.Method.POST)
            .execute()
        response = connection.parse()
        cookies.putAll(connection.cookies())
        return connection.statusCode()
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
//    println(scraper.getTitle())
//    println(scraper.getRequiredFormData())
}