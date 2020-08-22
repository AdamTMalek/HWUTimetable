package com.github.hwutimetable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.hwutimetable.scraper.Option

class CourseListAdapter(val courses: MutableSet<Option>) : RecyclerView.Adapter<CourseListAdapter.CourseViewHolder>() {

    class CourseViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private lateinit var courseOption: Option
        fun bind(courseOption: Option) {
            this.courseOption = courseOption

            view.findViewById<TextView>(R.id.course_name).text = courseOption.text
            view.findViewById<ImageView>(R.id.remove_course).setOnClickListener {
                removeSelf()
            }
        }

        private fun removeSelf() {
            val parent = view.parent as RecyclerView
            val position = parent.getChildAdapterPosition(view)
            val adapter = parent.adapter as CourseListAdapter

            with(adapter) {
                courses.remove(courseOption)
                adapter.notifyItemRemoved(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        return CourseViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.course_list_item, parent, false)
        )
    }

    override fun getItemCount() = courses.size

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(courses.elementAt(position))
    }
}