package com.github.hwutimetable

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Context
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
import com.github.hwutimetable.BuildConfig
import com.github.hwutimetable.changelog.ChangeLog
import com.github.hwutimetable.databinding.ActivityMainBinding
import com.github.hwutimetable.extensions.getSharedPreferences
import com.github.hwutimetable.filehandler.TimetableFileHandler
import com.github.hwutimetable.network.NetworkUtilities
import com.github.hwutimetable.network.NetworkUtils
import com.github.hwutimetable.parser.Timetable
import com.github.hwutimetable.settings.SettingsActivity
import com.github.hwutimetable.setup.SetupActivity
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import java.io.FileNotFoundException
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NetworkUtilities.ConnectivityCallbackReceiver {
    private val infoList = mutableListOf<Timetable.Info>()
    private lateinit var listAdapter: InfoListAdapter
    private lateinit var alertDialog: AlertDialog.Builder
    private var isAddMenuShowing = false

    @Inject
    lateinit var timetableHandler: TimetableFileHandler
    private val connectivityCallback: NetworkUtilities.ConnectivityCallback by lazy {
        NetworkUtilities.ConnectivityCallback(applicationContext!!)
    }

    @Inject
    lateinit var networkUtilities: NetworkUtils

    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        setSupportActionBar(viewBinding.toolbar)

        viewBinding.contentMain.recyclerView.layoutManager = LinearLayoutManager(this)
        initializeListAdapter()
        onInfoListChange()
        addTouchCallbacksHandler()
        listTimetables()
        setupAlertDialog()
        setupAddButtons()
        setTitle(R.string.main_activity_title)

        connectivityCallback.registerCallbackReceiver(this)
        if (!networkUtilities.hasInternetConnection()) {
            onConnectionLost()
        }

        NoOptimizationRequest(this).run {
            if (shouldRequestNoOptimization()) {
                requestNoOptimizations()
            }
        }

        ChangeLog(this).run {
            showRecentIfAfterUpdate()
        }

        updateLastRanVersion()
    }

    override fun onResume() {
        super.onResume()
        val appManager = AppManager(this)
        if (appManager.isFirstRun) {
            appManager.setFirstRunToFalse()
            openSetup()
        }
        reloadInfoList()
    }

    private fun updateLastRanVersion() {
        val sharedPref = getSharedPreferences(R.string.shared_pref_file_key, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt(getString(R.string.last_ran_version), BuildConfig.VERSION_CODE)
            apply()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun openSetup() {
        val intent = Intent(this, SetupActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
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
        dragTouchHelper.attachToRecyclerView(viewBinding.contentMain.recyclerView)

        val swipeToDeleteCallback = object : SwipeToDeleteCallback(applicationContext) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewHolder.setIsRecyclable(false)
                deleteItemAtPosition(viewHolder.adapterPosition)
            }
        }

        val touchHelper = ItemTouchHelper(swipeToDeleteCallback)
        touchHelper.attachToRecyclerView(viewBinding.contentMain.recyclerView)
        viewBinding.contentMain.recyclerView.addOnItemTouchListener(
            RecyclerItemClickListener(
                applicationContext, viewBinding.contentMain.recyclerView,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        onItemClick(position)
                    }

                    override fun onItemLongClick(view: View, position: Int) {
                        onItemLongClick(position)
                    }
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
        viewBinding.contentMain.recyclerView.adapter = listAdapter
    }

    private fun onItemClick(position: Int) {
        val timetableTitle = getRecyclerViewItemTitleView(position).text

        val intent = Intent(this, TimetableViewActivity::class.java)
        val info = timetableHandler.getStoredTimetables().find { it.name == timetableTitle }!!

        val timetable = timetableHandler.getTimetable(info)
        intent.putExtra("timetable", timetable)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

    private fun onItemLongClick(position: Int) {
        ItemContextMenu(this).create { action ->
            when (action) {
                ItemContextMenu.Action.RENAME -> renameItemAtPosition(position)
                ItemContextMenu.Action.DELETE -> deleteItemAtPosition(position)
            }
        }.show()
    }

    private fun renameItemAtPosition(position: Int) {
        val titleView = getRecyclerViewItemTitleView(position)
        val currentName = titleView.text
        RenameTimetableDialog.showDialog(this, currentName) { newName ->
            val timetableInfo = infoList.find { it.name == currentName }!!
            timetableInfo.name = newName
            titleView.text = newName
            timetableHandler.updateName(timetableInfo)
        }
    }

    private fun deleteItemAtPosition(position: Int) {
        val title = getRecyclerViewItemTitleView(position).text
        val info = timetableHandler.getStoredTimetables().find { it.name == title }!!

        var success = true
        try {
            timetableHandler.deleteTimetable(info)
        } catch (ex: FileNotFoundException) {
            success = false
        }

        val toastMessage = when (success) {
            true -> "Successfully deleted "
            false -> "Failed to delete "
        }.plus(title)

        infoList.removeAt(position)

        listAdapter.notifyItemRemoved(position)
        onInfoListChange()
        Toast.makeText(applicationContext, toastMessage, Toast.LENGTH_SHORT).show()
    }

    private fun getRecyclerViewItemTitleView(position: Int): TextView {
        return viewBinding.contentMain.recyclerView.findViewHolderForAdapterPosition(position)!!
            .itemView.findViewById(R.id.timetable_title)
    }

    private fun getTimetablesInfoList(): List<Timetable.Info> {
        return timetableHandler.getStoredTimetables()
    }

    private fun setupAlertDialog() {
        val listener = DialogInterface.OnClickListener { _, which -> handleDialogClick(which) }
        alertDialog = AlertDialog.Builder(this)
        alertDialog.setMessage(getString(R.string.delete_all_confirmation))
            .setPositiveButton("Yes", listener)
            .setNegativeButton("No", listener)
    }

    private fun setupAddButtons() {
        setAddButtonsBackground()
        hideAddMenu()
        viewBinding.addTimetable.setOnClickListener {
            if (isAddMenuShowing)
                hideAddMenu()
            else
                showAddMenu()
        }

        viewBinding.addProgramme.setOnClickListener {
            openAddProgrammeTimetable()
        }

        viewBinding.addCourse.setOnClickListener {
            openAddCoursesTimetable()
        }
    }

    private fun showAddMenu() {
        isAddMenuShowing = true
        showAddButton(viewBinding.addProgramme, -resources.getDimension(R.dimen.top_fab))
        showAddButton(viewBinding.addCourse, -resources.getDimension(R.dimen.mid_fab))
        viewBinding.addTimetable.icon = resources.getDrawable(R.drawable.ic_arrow_drop_down, applicationContext.theme)
    }

    private fun hideAddMenu() {
        isAddMenuShowing = false
        hideAddButton(viewBinding.addProgramme)
        hideAddButton(viewBinding.addCourse)
        viewBinding.addTimetable.icon = resources.getDrawable(R.drawable.ic_arrow_drop_up, applicationContext.theme)
    }

    private fun setAddButtonsBackground() {
        fun getColorStateList() = applicationContext.getColorStateList(R.color.fab_color)

        listOf(viewBinding.addProgramme, viewBinding.addCourse, viewBinding.addTimetable).forEach { button ->
            button.backgroundTintList = getColorStateList()
        }
    }

    private fun showAddButton(button: ExtendedFloatingActionButton, translation: Float) {
        button.extend()
        button.show()
        button.animate().translationY(translation)
        button.animate().alpha(1f)
    }

    private fun hideAddButton(button: ExtendedFloatingActionButton) {
        button.shrink()
        button.animate().translationY(0f)
        button.animate().alpha(0f)
    }

    private fun openAddProgrammeTimetable() {
        val intent = Intent(this, AddProgrammeTimetableActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

    private fun openAddCoursesTimetable() {
        val intent = Intent(this, AddCourseActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
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
        viewBinding.contentMain.noTimetablesText.visibility = View.VISIBLE
    }

    private fun hideNoTimetablesPlaceholder() {
        viewBinding.contentMain.noTimetablesText.visibility = View.INVISIBLE
    }

    override fun onConnectionAvailable() {
        runOnUiThread {
            viewBinding.addTimetable.isEnabled = true
        }
    }

    override fun onConnectionLost() {
        runOnUiThread {
            viewBinding.addTimetable.isEnabled = false
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
