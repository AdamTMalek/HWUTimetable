package com.github.hwutimetable.scraper

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import javax.inject.Inject

class ProgrammeScraper @Inject constructor() : Scraper(), ProgrammeTimetableScraper {
    override fun getTLinkType() = "studentsets"

    override fun getDLType() = "individual;swsurl;SWSCUST Student Set Individual"

    /**
     * Go to the Student Group Timetables (equivalent of clicking Student Groups link)
     * @return status code after transition
     */
    override suspend fun goToProgrammesTimetables(): Int {
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

    private suspend fun filterByLevel(department: String, level: String): Int {
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

        return submitForm(defaultUrl, formData).statusCode()
    }

    /**
     * Filter by department and then by level
     * @return HTTP status code
     */
    private suspend fun filter(department: String, level: String): Int {
        check(canApplyFilters()) {
            "Cannot apply filters unless on timetables site"
        }

        val departmentFilterStatusCode = filterByDepartment(department, true)

        if (departmentFilterStatusCode != 200)
            return departmentFilterStatusCode

        val levelFilterStatusCode = filterByLevel(department, level)

        state = ScraperState.Filtered
        return departmentFilterStatusCode
    }

    /**
     * Scrapes groups from the Student Group Timetable site
     * @param filters Filters to apply
     * @return List of group options
     */
    override suspend fun getGroups(filters: Map<String, Any>): List<Option> {
        check(canApplyFilters()) {
            "Illegal state for getGroups. Must be OnTimetablesSite or Filter."
        }

        val department = filters.getValue("department") as String
        val level = filters.getValue("level") as String

        // Apply the filter
        filter(department, level)
        state = ScraperState.Filtered
        this.department = department
        this.level = level

        return mapSelectionToOptions("#dlObject")
            ?: throw ScraperException("Could not find group options")
    }

    /**
     * Get the timetable document from the website with the given group id (option value)
     * and the semester.
     * @param filters Filters to apply
     * @return HTML document with the timetable
     */
    override suspend fun getTimetable(filters: Map<String, Any>): Document {
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
            return getTimetable(filters)
        }

        val formData = getTimetableFormData(filters["semester"] as Int).apply {
            putAll(
                mapOf(
                    "__EVENTARGUMENT" to "",
                    "__EVENTTARGET" to "",
                    "dlFilter2" to department,
                    "dlFilter" to level,
                    "dlObject" to filters["group"] as String,
                    "bGetTimetable" to "View Timetable"
                )
            )
        }

        submitForm(defaultUrl, formData)
        state = ScraperState.Finished

        response = Jsoup.parse(response!!.outerHtml())
        return response as Document
    }
}