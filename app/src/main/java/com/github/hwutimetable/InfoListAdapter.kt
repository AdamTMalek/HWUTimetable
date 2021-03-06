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
class InfoListAdapter(private val infoList: MutableList<Timetable.Info>) :
    RecyclerView.Adapter<InfoListAdapter.InfoListViewHolder>() {

    class InfoListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(info: Timetable.Info) {
            val title = view.findViewById<TextView>(R.id.timetable_title)
            title.text = info.name
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