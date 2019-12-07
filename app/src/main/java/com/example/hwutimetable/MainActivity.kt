package com.example.hwutimetable

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.DialogInterface
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
    private lateinit var alertDialog: AlertDialog.Builder
    private lateinit var docHandler: DocumentHandler

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
        setupAlertDialog()

        docHandler = DocumentHandler(this)
    }

    override fun onResume() {
        super.onResume()
        reloadInfoList()
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
            R.id.action_delete -> { alertDialog.show(); true }
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

                    override fun onItemLongClick(view: View, position: Int) {}
                })
        )
    }

    private fun listTimetables() {
        infoList.addAll(getTimetablesInfoList())
        if (infoList.isEmpty())
            return

        no_timetables_text.visibility = View.INVISIBLE

        initializeListAdapter()
        recycler_view.adapter = listAdapter
    }

    private fun reloadInfoList() {
        // Check if the list was empty to begin with
        // then, the no_timetables_text will exist
        if (infoList.isEmpty()) {
            no_timetables_text.visibility = View.INVISIBLE
        }
        infoList.clear()
        infoList.addAll(docHandler.getStoredTimetables())

        initializeListAdapter()
        listAdapter.notifyDataSetChanged()
    }

    private fun initializeListAdapter() {
        if (::listAdapter.isInitialized)
            return

        listAdapter = InfoListAdapter(infoList)
    }

    private fun onItemClick(position: Int) {
        val string = getTextFromRecyclerViewItem(position)

        val intent = Intent(this, ViewTimetable::class.java)
        intent.putExtra("timetable", string)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

    private fun onItemSwiped(position: Int) {
        val string = getTextFromRecyclerViewItem(position)
        val result = docHandler.deleteTimetable(string)

        val toastMessage = when (result) {
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
        return docHandler.getStoredTimetables()
    }

    private fun setupAlertDialog() {
        val listener = DialogInterface.OnClickListener { _, which -> handleDialogClick(which) }
        alertDialog = AlertDialog.Builder(this)
        alertDialog.setMessage("Are you sure?")
            .setPositiveButton("Yes", listener)
            .setNegativeButton("No", listener)
    }

    private fun handleDialogClick(which: Int) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            deleteAllTimetables()
        }
    }

    private fun deleteAllTimetables() {
        val deleted = docHandler.deleteAllTimetables()
        infoList.removeAll(deleted)

        val message = when (infoList.isEmpty()) {
            true -> "Successfully deleted all timetables"
            false -> "Something went wrong. Not all timetables were deleted"
        }

        docHandler.getStoredTimetables()
        listAdapter.notifyDataSetChanged()
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
