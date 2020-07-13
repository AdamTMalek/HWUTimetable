package com.github.hwutimetable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.hwutimetable.parser.Timetable


/**
 * This class is the adapter for representing list of timetables
 * in the [RecyclerView].
 */
class InfoListAdapter(private val infoList: MutableList<Timetable.TimetableInfo>) :
    RecyclerView.Adapter<InfoListAdapter.InfoListViewHolder>() {

    class InfoListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(timetableInfo: Timetable.TimetableInfo) {
            val title = view.findViewById<TextView>(R.id.timetable_title)
            val semester = view.findViewById<TextView>(R.id.semester_circle)
            title.text = timetableInfo.name
            semester.text = if (timetableInfo.semester.number == 1) "I" else "II"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoListViewHolder {
        return InfoListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.timetable_list_item, parent, false)
        )
    }

    override fun getItemCount() = infoList.size

    override fun onBindViewHolder(holder: InfoListViewHolder, position: Int) {
        holder.bind(infoList[position])
    }
}