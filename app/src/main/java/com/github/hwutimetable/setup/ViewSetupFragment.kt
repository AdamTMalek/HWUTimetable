package com.github.hwutimetable.setup

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import com.github.hwutimetable.R


/**
 * The [ViewSetupFragment] is a [Fragment] meant to be displayed
 * in the setup activity.
 * It displays both the original view and the simplified view, as well
 * as a preference screen using [ViewSetupFragment.ViewPreferenceFragment].
 */
class ViewSetupFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_setup_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Use the child fragment manager and attach the ViewPreferenceFragment
        val viewPreferenceFragment = ViewPreferenceFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.view_preference_container, viewPreferenceFragment)
            .commit()
        initialiseViews(view)
    }

    private fun initialiseViews(view: View) {
        val timetableViewIds = setOf(R.id.original_layout, R.id.simplified_layout)
        for (id in timetableViewIds) {
            val timetableView = view.findViewById<View>(id)
            setTimetableViewValues(timetableView)
        }
    }

    /**
     * This function takes a timetable view and populates the information
     * with made-up details (just for visualisation purpose).
     * Additionally, the colour of the class is set to lecture-type colour.
     *
     * @param timetableView: Original timetable or simplified timetable view.
     */
    private fun setTimetableViewValues(timetableView: View) {
        with(timetableView) {
            background = ColorDrawable(context.getColor(R.color.type_lec))
            findViewById<TextView>(R.id.class_code)?.text = "F32AB-S1"
            findViewById<TextView>(R.id.class_weeks)?.text = "1-12"
            findViewById<TextView>(R.id.class_room)?.text = "EM404"
            findViewById<TextView>(R.id.class_name)?.text = "Maths for Engineers"
            findViewById<TextView>(R.id.class_lecturer)?.text = "Dr N.Oname"
            findViewById<TextView>(R.id.class_type)?.text = "Lecture"
        }
    }

    /**
     * The [ViewPreferenceFragment] is a [PreferenceFragmentCompat]
     * that uses a preference screen defined in [R.xml.class_view_preferences]
     */
    class ViewPreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.class_view_preferences, rootKey)
        }

    }
}