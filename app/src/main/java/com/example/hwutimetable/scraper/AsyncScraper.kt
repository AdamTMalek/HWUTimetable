package com.example.hwutimetable.scraper

import android.os.AsyncTask
import org.jsoup.nodes.Document

/**
 * This class is a helper class for the Scraper class that allows asynchronous scraping.
 * It provides methods that allow to use the scraper (stored privately) on a separate thread to the UI
 */
class AsyncScraper {
    private var scraper: Scraper? = null
    private var initCallback: ((Scraper?) -> Unit)? = null
    private var asyncFilter: AsyncFilter? = null

    /**
     * AsyncInitialisation is used to initialise the Scraper object.
     */
    internal class AsyncInitialisation(private val callback: (Scraper?) -> Unit) : AsyncTask<Void, Void, Scraper>() {

        override fun doInBackground(vararg params: Void?) = Scraper()

        override fun onPostExecute(result: Scraper?) {
            this.callback(result)
        }
    }

    /**
     * AsyncFilter applies filters and returns List of Group options
     */
    internal class AsyncFilter(private val scraper: Scraper, private val callback: (List<Option>?) -> Unit) :
        AsyncTask<String, Void, List<Option>?>() {
        override fun doInBackground(vararg params: String): List<Option>? {
            return scraper.getGroups(params[0], params[1])
        }

        override fun onPostExecute(result: List<Option>?) {
            this.callback(result)
        }
    }

    /**
     * AsyncTimetableRequest requests the timetable and returns it as Jsoup Document
     */
    internal class AsyncTimetableRequest(private val scraper: Scraper, private val callback: (Document?) -> Unit) :
        AsyncTask<GroupRequest, Void, Document?>() {
        override fun doInBackground(vararg params: GroupRequest): Document? {
            val groupRequest = params[0]
            return scraper.getTimetable(groupRequest.optionValue, groupRequest.semester)
        }

        override fun onPostExecute(result: Document?) {
            this.callback(result)
        }
    }
    /**
     * Initialises the scraper asynchronously
     * @param callback: Callback that will be invoked after the scraper is initialised.
     */
    fun initialise(callback: (Scraper?) -> Unit) {
        initCallback = callback
        // We will actually use our private callback, because we want to store the scraper
        AsyncInitialisation(::initialisationCallback).execute()
    }

    /**
     * Assigns the parameter [scraper] to the private variable, and invokes the set (by [initialise]) callback
     * @param scraper: Scraper object initialised.
     */
    private fun initialisationCallback(scraper: Scraper?) {
        this.scraper = scraper
        this.initCallback?.invoke(scraper)
    }

    /**
     * Applies filters and uses the callback to return the groups as a list of options
     * @param department: Department option value
     * @param level: Level option value
     * @param callback: Callback that accepts a list of options as parameter
     */
    fun filter(department: String, level: String, callback: (List<Option>?) -> Unit) {
        asyncFilter = this.scraper?.let { AsyncFilter(it, callback) }
        asyncFilter?.execute(department, level)
    }

    /**
     * Makes an asynchronous request to get the timetable for the chosen group
     * @param group: Group (programme) option value
     * @param semester: Semester - either 0 or 1
     * @param callback: Callback that accepts a Jsoup document of the timetable
     */
    fun requestGroup(group: String, semester: Int, callback: (Document?) -> Unit) {
        val asyncRequest = this.scraper?.let { AsyncTimetableRequest(it, callback)}
        asyncRequest?.execute(GroupRequest(group, semester))
    }
}
