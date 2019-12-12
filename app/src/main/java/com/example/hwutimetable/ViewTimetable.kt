package com.example.hwutimetable

import android.os.Bundle
import android.view.*
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.hwutimetable.parser.Timetable
import com.example.hwutimetable.parser.TimetableDay
import kotlinx.android.synthetic.main.activity_view_timetable.*
import kotlinx.android.synthetic.main.fragment_view_timetable.*
import kotlinx.android.synthetic.main.fragment_view_timetable.view.*
import java.util.*

class ViewTimetable : AppCompatActivity() {

    /**
     * The [androidx.viewpager.widget.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * androidx.fragment.app.FragmentStatePagerAdapter.
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_timetable)

        setSupportActionBar(toolbar)

        var timetable = intent.extras?.get("timetable") ?: throw Exception("Timetable (Intent Extra) has not been passed")
        timetable = timetable as Timetable
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager, timetable)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter
        container.currentItem = getCurrentDayIndex()
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
            return true
        }

        return super.onOptionsItemSelected(item)
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
    inner class SectionsPagerAdapter(fm: FragmentManager, private val timetable: Timetable) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1, timetable.days[position])
        }

        override fun getCount(): Int {
            return 5
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {
        private var gridLayout: ScrollView? = null

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

            val timetableView = TimetableView.getTimetableItemView(context!!, list)
            gridLayout = timetableView

            with(rootView.findViewById(R.id.constraintLayout) as ViewGroup) {
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
            val gridId = gridLayout!!.id

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
