package com.example.hwutimetable

import androidx.appcompat.app.AppCompatActivity

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.example.hwutimetable.parser.Parser
import com.example.hwutimetable.parser.TimetableItem

import kotlinx.android.synthetic.main.activity_view_timetable.*
import kotlinx.android.synthetic.main.fragment_view_timetable.*
import kotlinx.android.synthetic.main.fragment_view_timetable.view.*

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

        val code = intent.extras?.get("timetable") ?: throw Exception("Timetable (Intent Extra) has not been passed")
        val timetable = getTimetable(code as String)
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager, timetable)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter
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

    private fun getTimetable(title: String): ArrayList<List<TimetableItem>> {
        val doc = DocumentHandler.getTimetable(baseContext, title)
        val timetable = Parser(doc).parse()
        val arrayList = arrayListOf(
            timetable[0].toList(),
            timetable[1].toList(),
            timetable[2].toList(),
            timetable[3].toList(),
            timetable[4].toList()
        )
        return arrayList
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager, private val timetable: ArrayList<List<TimetableItem>>) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            val itemsOfDay = ArrayList<TimetableItem>()
            timetable[position].forEach {
                itemsOfDay.add(it)
            }
            return PlaceholderFragment.newInstance(position + 1, itemsOfDay)
        }

        override fun getCount(): Int {
            return 5
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {
        private var gridLayout: GridLayout? = null

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

            val list = arguments?.getParcelableArrayList<TimetableItem>(ARG_SECTION_ITEMS)
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
            private val ARG_SECTION_ITEMS = "section_items"

            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(sectionNumber: Int, timetableItems: ArrayList<TimetableItem>): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                args.putParcelableArrayList(ARG_SECTION_ITEMS, timetableItems)
                fragment.arguments = args
                return fragment
            }
        }
    }
}
