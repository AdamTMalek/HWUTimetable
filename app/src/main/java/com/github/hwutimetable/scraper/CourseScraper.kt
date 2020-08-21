package com.github.hwutimetable.scraper

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import javax.inject.Inject

class CourseScraper @Inject constructor() : Scraper(), CourseTimetableScraper {
    /**
     * Go to the Courses Timetables (equivalent of clicking Courses link)
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
                    "__EVENTTARGET" to "LinkBtn_modules"
                )
            )
        }

        val connection = submitForm(defaultUrl, formData)
        state = ScraperState.OnTimetablesSite

        return connection.statusCode()
    }

    override suspend fun getGroups(filters: Map<String, String>): List<Option> {
        val department = filters.getValue("department")

        // Apply the filter
        filterByDepartment(department)
        state = ScraperState.Filtered
        this.department = department

        return mapSelectionToOptions("#dlObject")
            ?: throw ScraperException("Could not find group options")
    }

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
            filterByDepartment(filters.getValue("department") as String)
            return getTimetable(filters)
        }

        val formData = getTimetableFormData(filters["semester"] as Int).apply {
            putAll(
                mapOf(
                    "__EVENTARGUMENT" to "",
                    "__EVENTTARGET" to "",
                    "dlFilter2" to department,
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