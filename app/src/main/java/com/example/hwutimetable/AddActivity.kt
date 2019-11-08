package com.example.hwutimetable

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.hwutimetable.scraper.AsyncScraper
import com.example.hwutimetable.scraper.Option

import com.example.hwutimetable.scraper.Scraper
import kotlinx.android.synthetic.main.activity_add.*

class AddActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private var asyncScraper = AsyncScraper()
    private var departments: List<Option>? = null
    private var levels: List<Option>? = null
    private var selectedDepartment: Option? = null
    private var selectedLevel: Option? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        setItemSelectedListener()

        asyncScraper.initialise(::scraperInitCallback)
    }

    /**
     * Sets the ItemSelectedListener of all spinners of this activity to this object
     */
    private fun setItemSelectedListener() {

        departments_spinner.onItemSelectedListener = this
        levels_spinner.onItemSelectedListener = this
        groups_spinner.onItemSelectedListener = this
    }

    /**
     * Initialisation callback for AsyncScraper initialisation
     */
    private fun scraperInitCallback(scraper: Scraper?) {
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
        asyncScraper.filter(selectedDepartment!!.optionValue, selectedLevel!!.optionValue, ::groupsCallback)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }
}
