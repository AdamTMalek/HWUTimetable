package com.github.hwutimetable.scraper

import org.jsoup.nodes.Document

interface TimetableScraper {
    /**
     * Sets up the scraper (performs login, goes to the appropriate page etc.)
     */
    suspend fun setup()

    /**
     * Get a list of the departments from the Student Group Timetables site
     * @return List of department options
     */
    fun getDepartments(): List<Option>

    /**
     * Scrapes groups from the Student Group Timetable site
     * @param filters Filters to apply
     * @return List of group options
     */
    suspend fun getGroups(filters: Map<String, Any>): List<Option>

    /**
     * Get the timetable document from the website with the given group id (option value)
     * and the semester.
     * @param filters Filters to apply
     * @return HTML document with the timetable
     */
    suspend fun getTimetable(filters: Map<String, Any>): Document
}

interface ProgrammeTimetableScraper : TimetableScraper {
    /**
     * Get a list of the levels from the Student Group Timetables site
     * @return List of level options
     */
    fun getLevels(): List<Option>
}

interface CourseTimetableScraper {

}
