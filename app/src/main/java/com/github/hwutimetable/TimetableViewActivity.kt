package com.github.hwutimetable

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
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.hwutimetable.databinding.ActivityViewTimetableBinding
import com.github.hwutimetable.databinding.FragmentViewTimetableBinding
import com.github.hwutimetable.filehandler.TimetableFileHandler
import com.github.hwutimetable.parser.Clashes
import com.github.hwutimetable.parser.Timetable
import com.github.hwutimetable.parser.TimetableDay
import com.github.hwutimetable.settings.SettingsActivity
import dagger.hilt.android.AndroidEntryPoint
import org.joda.time.DateTimeConstants
import org.joda.time.LocalTime
import javax.inject.Inject

@AndroidEntryPoint
class TimetableViewActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    /**
     * The [androidx.viewpager.widget.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * androidx.fragment.app.FragmentStatePagerAdapter.
     */
    private var sectionsPagerAdapter: SectionsPagerAdapter? = null

    @Inject
    lateinit var timetableHandler: TimetableFileHandler

    @Inject
    lateinit var dateProvider: CurrentDateProvider

    private lateinit var wholeTimetable: Timetable
    private val toolbar: Toolbar by lazy {
        findViewById(R.id.toolbar)
    }

    private lateinit var viewBinding: ActivityViewTimetableBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityViewTimetableBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        wholeTimetable = getTimetable(intent)
        val name = wholeTimetable.info.name
        setTimetableTitle(name)
        val currentWeek = wholeTimetable.info.semester.getWeek(dateProvider.getCurrentDate())
        populateSpinner(currentWeek)
        displayTimetableForWeek(currentWeek, true)
    }

    override fun onResume() {
        sectionsPagerAdapter?.notifyDataSetChanged()
        super.onResume()
    }

    private fun setTimetableTitle(name: String) {
        with(toolbar) {
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

        val currentPage = viewBinding.container.currentItem

        if (sectionsPagerAdapter == null) {
            sectionsPagerAdapter = SectionsPagerAdapter(this, timetable)
            viewBinding.container.adapter = sectionsPagerAdapter
        }

        with(viewBinding.container.adapter!! as SectionsPagerAdapter) {
            this.timetable = timetable
            this.notifyDataSetChanged()
        }

        val currentItemIndex = if (showToday) getCurrentDayIndex() else currentPage
        viewBinding.container.setCurrentItem(currentItemIndex, false)
    }

    private fun getTimetable(intent: Intent): Timetable {
        val timetable = intent.extras?.get("timetable")
            ?: throw Exception("Timetable (Intent Extra) has not been passed")

        return timetable as Timetable
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds timetableClasses to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_view_timetable, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.rename -> {
                showRenameTimetableDialog()
                true
            }
            R.id.action_settings -> {
                openSettings()
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showRenameTimetableDialog() {
        RenameTimetableDialog.showDialog(this, wholeTimetable.info.name) { name ->
            renameTimetable(name)
        }
    }

    private fun renameTimetable(newName: String) {
        wholeTimetable.info.name = newName
        toolbar.title = newName
        timetableHandler.updateName(wholeTimetable.info)
    }

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

    private fun populateSpinner(currentWeek: Int) {
        val weeks = (1..12).toList()
        val adapter = ArrayAdapter(this, R.layout.weeks_spinner_item, weeks)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        viewBinding.weeksSpinner.let {
            it.adapter = adapter
            it.setSelection(weeks.indexOf(currentWeek))
            it.onItemSelectedListener = this
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val containerId = parent?.id ?: return
        if (containerId != viewBinding.weeksSpinner.id) {
            return
        }

        val selectedWeek = viewBinding.weeksSpinner.selectedItem as Int
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
        val day = dateProvider.getCurrentDate().dayOfWeek

        // If the current day is Saturday or Sunday - show the timetable for Monday
        if (day == DateTimeConstants.SATURDAY || day == DateTimeConstants.SUNDAY)
            return 0

        // Monday is defined as 1 in the dayOfWeek in Joda LocalDate,
        // so we have to takeaway 1 for the index
        return day - 1
    }


    /**
     * A [FragmentStateAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    class SectionsPagerAdapter(fragmentActivity: FragmentActivity, var timetable: Timetable) :
        FragmentStateAdapter(fragmentActivity) {

        override fun createFragment(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1, timetable.days[position], timetable.info.startTime)
        }

        override fun getItemCount(): Int {
            return 5
        }

        override fun getItemId(position: Int): Long {
            return timetable.days[position].hashCode().toLong()
        }
    }

    private fun moveToPreviousDay() {
        viewBinding.container.setCurrentItem(viewBinding.container.currentItem - 1, true)
    }

    private fun moveToNextDay() {
        viewBinding.container.setCurrentItem(viewBinding.container.currentItem + 1, true)
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {
        private lateinit var gridLayout: ViewGroup
        private lateinit var rootView: View

        private lateinit var viewBinding: FragmentViewTimetableBinding
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            viewBinding = FragmentViewTimetableBinding.inflate(inflater, container, false)
            rootView = viewBinding.root
            val sectionNumber = arguments?.getInt(ARG_SECTION_NUMBER) ?: throw Exception("Missing section number")
            setCurrentDayLabelText(sectionNumber)
            setPreviousDayLabelText(sectionNumber)
            setNextDayLabelText(sectionNumber)

            val list = arguments?.getParcelable<TimetableDay>(ARG_SECTION_TIMETABLE)
                ?: throw Exception("TimetableClass list must not be null")

            val startTime = requireArguments().getSerializable(ARG_START_TIME) as LocalTime
            val viewGenerator = TimetableViewGenerator(requireContext(), startTime)
            val timetableView = viewGenerator.getTimetableItemView(list)
            gridLayout = timetableView

            with(rootView.findViewById(R.id.scroll_view) as ViewGroup) {
                addView(timetableView)
            }

            setPreviousDayClickListener()
            setNextDayClickListener()

            return viewBinding.root
        }

        private fun setCurrentDayLabelText(currentDay: Int) {
            rootView.findViewById<TextView>(R.id.section_label).text = getDay(currentDay)
        }

        private fun setPreviousDayLabelText(currentDay: Int) {
            val dayIndex = currentDay - 1
            rootView.findViewById<TextView>(R.id.previous_day_label).text = if (dayIndex >= 1) {
                getDay(dayIndex)
            } else {
                ""
            }
        }

        private fun setPreviousDayClickListener() {
            rootView.findViewById<TextView>(R.id.previous_day_label).setOnClickListener {
                (activity as TimetableViewActivity).moveToPreviousDay()
            }
        }

        private fun setNextDayLabelText(currentDay: Int) {
            val dayIndex = currentDay + 1
            rootView.findViewById<TextView>(R.id.next_day_label).text = if (dayIndex <= 5) {
                getDay(dayIndex)
            } else {
                ""
            }
        }

        private fun setNextDayClickListener() {
            rootView.findViewById<TextView>(R.id.next_day_label).setOnClickListener {
                (activity as TimetableViewActivity).moveToNextDay()
            }
        }

        private fun getDay(sectionNumber: Int) = when (sectionNumber) {
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            else -> throw IllegalArgumentException("ARG_SECTION_NUMBER must be between 1 and 5")
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
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
            constraintSet.applyTo(viewBinding.constraintLayout)
        }

        companion object {
            /**
             * The fragment argument representing the section number for this
             * fragment.
             */
            private const val ARG_SECTION_NUMBER = "section_number"

            /**
             * The fragment argument representing the timetable timetableClasses for this
             * fragment.
             */
            private const val ARG_SECTION_TIMETABLE = "section_items"

            private const val ARG_START_TIME = "start_time"

            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(sectionNumber: Int, timetableDay: TimetableDay, startTime: LocalTime): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                args.putParcelable(ARG_SECTION_TIMETABLE, timetableDay)
                args.putSerializable(ARG_START_TIME, startTime)
                fragment.arguments = args
                return fragment
            }
        }
    }
}
