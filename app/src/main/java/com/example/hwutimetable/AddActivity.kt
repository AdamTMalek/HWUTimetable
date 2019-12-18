package com.example.hwutimetable

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.hwutimetable.filehandler.TimetableFileHandler
import com.example.hwutimetable.filehandler.TimetableInfo
import com.example.hwutimetable.parser.Parser
import com.example.hwutimetable.scraper.AsyncScraper
import com.example.hwutimetable.scraper.Option
import com.example.hwutimetable.scraper.Scraper
import kotlinx.android.synthetic.main.activity_add.*
import org.jsoup.nodes.Document

class AddActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private var asyncScraper = AsyncScraper()
    private var departments: List<Option>? = null
    private var levels: List<Option>? = null
    private var groups: List<Option>? = null
    private var selectedDepartment: Option? = null
    private var selectedLevel: Option? = null
    private var requestedGroup: Option? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        setItemSelectedListener()
        setButtonClickListener()

        asyncScraper.initialise(::scraperInitCallback)
        changeProgressBarVisibility(true)
    }

    /**
     * Sets the ItemSelectedListener of all spinners of this activity to this object
     */
    private fun setItemSelectedListener() {
        departments_spinner.onItemSelectedListener = this
        levels_spinner.onItemSelectedListener = this
    }

    private fun setButtonClickListener() {
        submit_button.setOnClickListener {
            changeProgressBarVisibility(true)
            val groupOption = groups?.find {
                it.text == groups_spinner.selectedItem.toString()
            }

            if (groupOption != null) {
                requestedGroup = groupOption
                val optionValue = groupOption.optionValue
                val semester = getSemesterFromName((groupOption.text))
                asyncScraper.requestGroup(optionValue, semester, ::timetableRequestCallback)
            }
        }
    }

    private fun getSemesterFromName(name: String): Int {
        val regex = Regex("(?<=[Ss]emester )(\\d)")
        val match = regex.find(name) ?: throw IllegalStateException("Semester match is null!")
        return match.groups.first()!!.value.toInt()
    }

    /**
     * Initialisation callback for AsyncScraper initialisation
     */
    private fun scraperInitCallback(scraper: Scraper?) {
        changeProgressBarVisibility(false)

        scraper ?: return
        departments = scraper.getDepartments()
        levels = scraper.getLevels()

        // Put the data we just got to the Spinners
        departments?.map { it.text }?.let { applyAdapterFromList(departments_spinner, it) }
        levels?.map { it.text }?.let { applyAdapterFromList(levels_spinner, it) }
    }

    /**
     * getGroups callback for AsyncScraper
     */
    private fun groupsCallback(options: List<Option>?) {
        changeProgressBarVisibility(false)
        groups = options

        if (options != null) {
            applyAdapterFromList(groups_spinner, options.map { it.text })
            submit_button.isEnabled = true
        }
    }

    /**
     * Applies ArrayAdapter to the given list of strings and sets it to the given spinner
     * @param spinner: Spinner to which the new adapter should be set
     * @param list: List of strings
     */
    private fun applyAdapterFromList(spinner: Spinner, list: List<String>) {
        spinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list)
    }

    /**
     * Callback for all Spinners
     */
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            departments_spinner.id -> selectedDepartment = departments?.find {
                it.text == departments_spinner.selectedItem.toString()
            }
            levels_spinner.id -> selectedLevel = levels?.find {
                it.text == levels_spinner.selectedItem.toString()
            }
        }

        if (selectedDepartment == null || selectedLevel == null)
            return

        if (selectedDepartment!!.text == "(Any Department)" || selectedLevel!!.text == "(Any Level)")
            return // We require both filters

        // Both filters applied - get the groups
        changeProgressBarVisibility(true)
        asyncScraper.filter(selectedDepartment!!.optionValue, selectedLevel!!.optionValue, ::groupsCallback)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    private fun timetableRequestCallback(document: Document?) {
        checkNotNull(document) { return }
        checkNotNull(requestedGroup) { throw NullPointerException("requestedGroup cannot be null") }
        val timetableInfo = TimetableInfo(
            requestedGroup!!.optionValue,
            requestedGroup!!.text
        )

        val timetable = Parser(document).parse()
        val directory = applicationContext.filesDir

        if (saveTimetable()) {
            val fileHandler = TimetableFileHandler(directory)
            fileHandler.save(timetable, timetableInfo)
        }

        changeProgressBarVisibility(false)

        val intent = Intent(this, ViewTimetable::class.java)
        intent.putExtra("timetable", timetable)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

    private fun saveTimetable() = follow_checkbox.isChecked

    private fun changeProgressBarVisibility(visible: Boolean) {
        when (visible) {
            true -> progress_bar.visibility = View.VISIBLE
            false -> progress_bar.visibility = View.INVISIBLE
        }
    }
}
