package com.example.hwutimetable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hwutimetable.filehandler.TimetableInfo


/**
 * This class is the adapter for representing list of [TimetableInfo]
 * in the [RecyclerView].
 */
class InfoListAdapter(private val infoList: MutableList<TimetableInfo>) :
    RecyclerView.Adapter<InfoListAdapter.InfoListViewHolder>() {

    class InfoListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView = view as TextView

        fun bind(timetableInfo: TimetableInfo) {
            textView.text = timetableInfo.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoListViewHolder {
        return InfoListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_text_view, parent, false)
        )
    }

    override fun getItemCount() = infoList.size

    override fun onBindViewHolder(holder: InfoListViewHolder, position: Int) {
        holder.bind(infoList[position])
    }
}