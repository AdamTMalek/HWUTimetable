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
     * Get a list of the levels from the Student Group Timetables site
     * @return List of level options
     */
    fun getLevels(): List<Option>

    /**
     * Scrapes groups from the Student Group Timetable site
     * @param department department option's value (not text)
     * @param level level option's value (not text)
     * @return List of group options
     */
    suspend fun getGroups(department: String, level: String): List<Option>

    /**
     * Get the timetable document from the website with the given [group] id (option value)
     * and the semester.
     * @param group group Option value
     * @param semester Integer 1 or 2
     * @return HTML document with the timetable
     */
    suspend fun getTimetable(group: String, semester: Int): Document
}