package com.github.hwutimetable

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.hwutimetable.filehandler.TimetableFileHandler
import com.github.hwutimetable.network.NetworkUtilities
import com.github.hwutimetable.network.NetworkUtils
import com.github.hwutimetable.parser.Timetable
import com.github.hwutimetable.settings.SettingsActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.FileNotFoundException
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NetworkUtilities.ConnectivityCallbackReceiver {
    private val infoList = mutableListOf<Timetable.TimetableInfo>()
    private lateinit var listAdapter: InfoListAdapter
    private lateinit var alertDialog: AlertDialog.Builder

    @Inject
    lateinit var timetableHandler: TimetableFileHandler
    private val connectivityCallback: NetworkUtilities.ConnectivityCallback by lazy {
        NetworkUtilities.ConnectivityCallback(applicationContext!!)
    }

    @Inject
    lateinit var networkUtilities: NetworkUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        }
        fab.backgroundTintList = applicationContext.getColorStateList(R.color.fab_color)

        recycler_view.layoutManager = LinearLayoutManager(this)
        initializeListAdapter()
        onInfoListChange()
        addTouchCallbacksHandler()
        listTimetables()
        setupAlertDialog()

        setTitle(R.string.main_activity_title)

        connectivityCallback.registerCallbackReceiver(this)
        if (!networkUtilities.hasInternetConnection()) {
            onConnectionLost()
        }
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
            R.id.action_settings -> {
                openSettings(); true
            }
            R.id.action_delete -> {
                alertDialog.show(); true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

    private fun addTouchCallbacksHandler() {
        val dragCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                Collections.swap(infoList, from, to)
                recyclerView.adapter?.notifyItemMoved(from, to)
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                return
            }

            override fun isLongPressDragEnabled(): Boolean {
                return true
            }
        }

        val dragTouchHelper = ItemTouchHelper(dragCallback)
        dragTouchHelper.attachToRecyclerView(recycler_view)

        val swipeToDeleteCallback = object : SwipeToDeleteCallback(applicationContext) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewHolder.setIsRecyclable(false)
                onItemSwiped(viewHolder.adapterPosition)
            }
        }

        val touchHelper = ItemTouchHelper(swipeToDeleteCallback)
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

        hideNoTimetablesPlaceholder()
    }

    private fun reloadInfoList() {
        infoList.clear()
        infoList.addAll(timetableHandler.getStoredTimetables())

        initializeListAdapter()
        onInfoListChange()
    }

    private fun initializeListAdapter() {
        if (::listAdapter.isInitialized)
            return

        listAdapter = InfoListAdapter(infoList)
        recycler_view.adapter = listAdapter
    }

    private fun onItemClick(position: Int) {
        val string = getTextFromRecyclerViewItem(position)

        val intent = Intent(this, TimetableViewActivity::class.java)
        val info = timetableHandler.getStoredTimetables().find { it.name == string }!!

        val timetable = timetableHandler.getTimetable(info)
        intent.putExtra("timetable", timetable)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

    private fun onItemSwiped(position: Int) {
        val string = getTextFromRecyclerViewItem(position)
        val info = timetableHandler.getStoredTimetables().find { it.name == string }!!

        var success = true
        try {
            timetableHandler.deleteTimetable(info)
        } catch (ex: FileNotFoundException) {
            success = false
        }

        val toastMessage = when (success) {
            true -> "Successfully deleted "
            false -> "Failed to delete "
        }.plus(string)

        infoList.removeAt(position)

        listAdapter.notifyItemRemoved(position)
        onInfoListChange()
        Toast.makeText(applicationContext, toastMessage, Toast.LENGTH_SHORT).show()
    }

    private fun getTextFromRecyclerViewItem(position: Int): String {
        return recycler_view.findViewHolderForAdapterPosition(position)!!
            .itemView.findViewById<TextView>(R.id.timetable_title).text as String
    }

    private fun getTimetablesInfoList(): List<Timetable.TimetableInfo> {
        return timetableHandler.getStoredTimetables()
    }

    private fun setupAlertDialog() {
        val listener = DialogInterface.OnClickListener { _, which -> handleDialogClick(which) }
        alertDialog = AlertDialog.Builder(this)
        alertDialog.setMessage(getString(R.string.delete_all_confirmation))
            .setPositiveButton("Yes", listener)
            .setNegativeButton("No", listener)
    }

    private fun handleDialogClick(which: Int) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            deleteAllTimetables()
        }
    }

    private fun deleteAllTimetables() {
        val deleted = timetableHandler.deleteAllTimetables()
        infoList.removeAll(deleted)

        val message = when (infoList.isEmpty()) {
            true -> "Successfully deleted all timetables"
            false -> "Something went wrong. Not all timetables were deleted"
        }

        onInfoListChange()
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun onInfoListChange() {
        listAdapter.notifyDataSetChanged()
        if (infoList.isEmpty())
            displayNoTimetablesPlaceholder()
        else
            hideNoTimetablesPlaceholder()
    }

    private fun displayNoTimetablesPlaceholder() {
        no_timetables_text.visibility = View.VISIBLE
    }

    private fun hideNoTimetablesPlaceholder() {
        no_timetables_text.visibility = View.INVISIBLE
    }

    override fun onConnectionAvailable() {
        runOnUiThread {
            fab.isEnabled = true
        }
    }

    override fun onConnectionLost() {
        runOnUiThread {
            fab.isEnabled = false
        }
    }

    override fun onDestroy() {
        connectivityCallback.cleanup()
        super.onDestroy()
    }

    override fun onStop() {
        timetableHandler.saveOrder(infoList)
        super.onStop()
    }
}
