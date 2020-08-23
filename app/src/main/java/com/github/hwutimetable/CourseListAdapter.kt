package com.github.hwutimetable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.hwutimetable.scraper.Option

class CourseListAdapter(val courses: MutableSet<Option>) : RecyclerView.Adapter<CourseListAdapter.CourseViewHolder>() {
    private var onElementRemovedListener: OnElementRemovedListener? = null

    fun interface OnElementRemovedListener {
        fun onElementRemoved(element: Option)
    }

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
                callOnElementRemoved(courseOption)
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

    private fun callOnElementRemoved(option: Option) {
        if (onElementRemovedListener != null)
            onElementRemovedListener!!.onElementRemoved(option)
    }

    fun setOnElementRemovedListener(listener: OnElementRemovedListener) {
        onElementRemovedListener = listener
    }
}