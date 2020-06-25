package com.example.hwutimetable

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.example.hwutimetable.parser.Clashes
import com.example.hwutimetable.parser.Timetable
import com.example.hwutimetable.parser.TimetableDay
import com.example.hwutimetable.settings.SettingsActivity
import kotlinx.android.synthetic.main.activity_view_timetable.*
import kotlinx.android.synthetic.main.fragment_view_timetable.*
import kotlinx.android.synthetic.main.fragment_view_timetable.view.*
import org.joda.time.LocalDate
import java.util.*

class ViewTimetable : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    /**
     * The [androidx.viewpager.widget.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * androidx.fragment.app.FragmentStatePagerAdapter.
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private lateinit var wholeTimetable: Timetable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_timetable)

        setSupportActionBar(toolbar)

        val name = getTimetableName(intent)
        setTimetableTitle(name)

        wholeTimetable = getTimetable(intent)
        val currentWeek = wholeTimetable.semester.getWeek(LocalDate.now())
        populateSpinner(currentWeek)
        displayTimetableForWeek(currentWeek, true)
    }

    private fun setTimetableTitle(name: String) {
        with(findViewById<Toolbar>(R.id.toolbar)) {
            title = name

            children.forEach { child ->
                if (child is TextView) {
                    child.ellipsize = TextUtils.TruncateAt.MARQUEE
                    child.isSelected = true
                    child.marqueeRepeatLimit = -1
                }
            }
        }
    }

    private fun displayTimetableForWeek(week: Int, showToday: Boolean) {
        val timetable = wholeTimetable.getForWeek(week)
        val clashes = timetable.getClashes(week)
        if (!clashes.isEmpty())
            displayClashesDialog(clashes)

        val currentPage = container.currentItem

        if (mSectionsPagerAdapter == null) {
            mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager, timetable)
            container.adapter = mSectionsPagerAdapter
        }

        with(container.adapter!! as SectionsPagerAdapter) {
            this.timetable = timetable
            this.notifyDataSetChanged()
        }

        if (showToday)
            container.currentItem = getCurrentDayIndex()
        else
            container.currentItem = currentPage
    }

    private fun getTimetable(intent: Intent): Timetable {
        val timetable = intent.extras?.get("timetable")
            ?: throw Exception("Timetable (Intent Extra) has not been passed")

        return timetable as Timetable
    }

    private fun getTimetableName(intent: Intent): String {
        val info = intent.extras?.get("name")
            ?: throw Exception("Timetable name (Intent Extra) has not been passed")

        return info as String
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_view_timetable, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        }

        return super.onOptionsItemSelected(item)
    }

    private fun populateSpinner(currentWeek: Int) {
        val weeks = (1..12).toList()
        val adapter = ArrayAdapter<Int>(this, R.layout.weeks_spinner_item, weeks)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        weeks_spinner.adapter = adapter
        weeks_spinner.setSelection(weeks.indexOf(currentWeek))
        weeks_spinner.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val containerId = parent?.id ?: return
        if (containerId != weeks_spinner.id) {
            return
        }

        val selectedWeek = weeks_spinner.selectedItem as Int
        displayTimetableForWeek(selectedWeek, false)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    private fun displayClashesDialog(clashes: Clashes) {
        val builder = AlertDialog.Builder(this)
        val daysOfClashes = clashes.getClashes()
            .groupBy { it.day }.keys
            .joinToString(prefix = "", postfix = "", separator = ",")
        builder.setTitle("Clashes")
            .setMessage("There are clashes for the following days: $daysOfClashes")
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.cancel()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun getCurrentDayIndex(): Int {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

        // If the current day is Saturday or Sunday - show the timetable for Monday
        if (day == Calendar.SATURDAY || day == Calendar.SUNDAY)
            return 0

        // Monday is actually 2 in the Java's enum
        // since their first day of the week (1) is Sunday
        // we need to takeaway 2 to get the current day as index
        return day - 2
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager, var timetable: Timetable) :
        FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1, timetable.days[position])
        }

        override fun getItemPosition(`object`: Any): Int {
            return PagerAdapter.POSITION_NONE
        }

        override fun getCount(): Int {
            return 5
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {
        private lateinit var gridLayout: ViewGroup
        private val viewGenerator by lazy {
            TimetableViewGenerator(context!!)
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val rootView = inflater.inflate(R.layout.fragment_view_timetable, container, false)
            rootView.section_label.text = when (arguments?.getInt(ARG_SECTION_NUMBER)) {
                1 -> "Monday"
                2 -> "Tuesday"
                3 -> "Wednesday"
                4 -> "Thursday"
                5 -> "Friday"
                else -> throw IllegalArgumentException("ARG_SECTION_NUMBER must be between 0 and 4")
            }

            val list = arguments?.getParcelable<TimetableDay>(ARG_SECTION_TIMETABLE)
                ?: throw Exception("TimetableItem list must not be null")

            val timetableView = viewGenerator.getTimetableItemView(list)
            gridLayout = timetableView

            with(rootView.findViewById(R.id.scroll_view) as ViewGroup) {
                addView(timetableView)
            }

            return rootView
        }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            addConstraints()
        }

        private fun addConstraints() {
            val constraintSet = ConstraintSet()
            val gridId = gridLayout.id

            constraintSet.clone(context, R.layout.fragment_view_timetable)
            constraintSet.connect(gridId, ConstraintSet.TOP, R.id.section_label, ConstraintSet.BOTTOM)
            constraintSet.connect(gridId, ConstraintSet.BOTTOM, R.id.constraintLayout, ConstraintSet.BOTTOM)
            constraintSet.connect(gridId, ConstraintSet.LEFT, R.id.constraintLayout, ConstraintSet.LEFT)
            constraintSet.connect(gridId, ConstraintSet.RIGHT, R.id.constraintLayout, ConstraintSet.RIGHT)
            constraintSet.applyTo(constraintLayout)
        }

        companion object {
            /**
             * The fragment argument representing the section number for this
             * fragment.
             */
            private val ARG_SECTION_NUMBER = "section_number"

            /**
             * The fragment argument representing the timetable items for this
             * fragment.
             */
            private val ARG_SECTION_TIMETABLE = "section_items"

            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(sectionNumber: Int, timetableDay: TimetableDay): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                args.putParcelable(ARG_SECTION_TIMETABLE, timetableDay)
                fragment.arguments = args
                return fragment
            }
        }
    }
}
