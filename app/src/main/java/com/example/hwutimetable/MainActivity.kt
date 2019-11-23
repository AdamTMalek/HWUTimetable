package com.example.hwutimetable

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hwutimetable.filehandler.DocumentHandler
import com.example.hwutimetable.filehandler.TimetableInfo

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {
    private val infoList = mutableListOf<TimetableInfo>()
    private lateinit var listAdapter: InfoListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        }
        recycler_view.layoutManager = LinearLayoutManager(this)
        addTouchCallbacksHandler()
        listTimetables()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addTouchCallbacksHandler() {
        val callback = object : SwipeToDeleteCallback(applicationContext) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewHolder.setIsRecyclable(false)
                onItemSwiped(viewHolder.adapterPosition)
            }
        }

        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(recycler_view)
        recycler_view.addOnItemTouchListener(
            RecyclerItemClickListener(applicationContext, recycler_view,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        onItemClick(position)
                    }

                    override fun onItemLongClick(view: View, position: Int) { }
                })
        )
    }

    private fun listTimetables() {
        infoList.addAll(getTimetablesInfoList())
        if (infoList.isEmpty())
            return

        no_timetables_text.visibility = View.INVISIBLE
        listAdapter = InfoListAdapter(infoList)

        recycler_view.adapter = listAdapter
    }

    private fun onItemClick(position: Int) {
        val string = getTextFromRecyclerViewItem(position)

        val intent = Intent(this, ViewTimetable::class.java)
        intent.putExtra("timetable", string)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

    private fun onItemSwiped(position: Int) {
        val string = getTextFromRecyclerViewItem(position)
        val result = DocumentHandler.deleteTimetable(applicationContext, string)

        val toastMessage = when(result) {
            true -> "Successfully deleted "
            false -> "Failed to delete "
        }.plus(string)

        infoList.removeAt(position)
        listAdapter.notifyItemRemoved(position)
        listAdapter.notifyDataSetChanged()
        Toast.makeText(applicationContext, toastMessage, Toast.LENGTH_SHORT).show()
    }

    private fun getTextFromRecyclerViewItem(position: Int): String {
        return recycler_view.findViewHolderForAdapterPosition(position)!!
            .itemView.findViewById<TextView>(R.id.list_text_view).text as String
    }

    private fun getTimetablesInfoList(): List<TimetableInfo> {
        return DocumentHandler.getStoredTimetables(applicationContext)
    }
}
