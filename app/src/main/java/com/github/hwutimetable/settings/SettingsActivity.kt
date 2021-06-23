package com.github.hwutimetable.settings

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.github.hwutimetable.R
import com.github.hwutimetable.changelog.ChangeLog
import com.github.hwutimetable.network.NetworkUtilities
import com.github.hwutimetable.parser.Parser
import com.github.hwutimetable.parser.Timetable
import com.github.hwutimetable.scraper.Scraper
import com.github.hwutimetable.setup.SetupActivity
import com.github.hwutimetable.updater.OnUpdateFinishedListener
import com.github.hwutimetable.updater.UpdateManager
import com.github.hwutimetable.updater.UpdateNotifier
import com.github.hwutimetable.updater.Updater
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class SettingsActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.settings,
                SettingsFragment()
            )
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, pref.fragment).apply {
            arguments = args
        }

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.settings, fragment)
            .addToBackStack(null)
            .commit()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed(); true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    @DelicateCoroutinesApi
    class SettingsFragment : PreferenceFragmentCompat(),
        OnUpdateFinishedListener, NetworkUtilities.ConnectivityCallbackReceiver {
        private lateinit var updateManager: UpdateManager
        private val networkUtilities: NetworkUtilities by lazy {
            NetworkUtilities(this.requireContext())
        }
        private val updateNowPreference: Preference by lazy {
            findPreference(getString(R.string.update_now))!!
        }

        private val connectivityCallback: NetworkUtilities.ConnectivityCallback by lazy {
            NetworkUtilities.ConnectivityCallback(requireContext())
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            updateManager = UpdateManager(requireContext())

            setUpdateButtonHandler()
            setRunSetupButtonHandler()
            setVersionPreferenceSummary()
            setRecentChangesClickHandler()

            connectivityCallback.registerCallbackReceiver(this)

            if (!networkUtilities.hasInternetConnection())
                onConnectionLost()
        }

        override fun onDisplayPreferenceDialog(preference: Preference?) {
            when (preference) {
                is TimePreference -> displayTimePreference(preference)
                is NumberPreference -> displayNumberPreference(preference)
                else -> super.onDisplayPreferenceDialog(preference)
            }
        }

        private fun displayTimePreference(preference: Preference) {
            TimePreferenceDialogFragmentCompat.newInstance(preference.key)
                .show(parentFragmentManager, "androidx.support.preference.PreferenceFragment.DIALOG")
        }

        private fun displayNumberPreference(preference: Preference) {
            NumberPreferenceDialogFragmentCompat.newInstance(preference.key)
                .show(parentFragmentManager, "androidx.support.preference.PreferenceFragment.DIALOG")
        }

        private fun setUpdateButtonHandler() {
            val button = findPreference<Preference>(getString(R.string.update_now))
            button!!.setOnPreferenceClickListener {
                startUpdate()
                return@setOnPreferenceClickListener true
            }
        }

        /**
         * Starts the update-check process using [Scraper] and [Parser], as well as [UpdateNotifier]
         * to show "update in progress" notification.
         */
        private fun startUpdate() {
            val activity = this.requireActivity()
            val context = activity.applicationContext
            val updater = Updater(activity.filesDir, activity)
            val notifier = UpdateNotifier(context)

            updater.addInProgressListener(notifier)
            updater.addFinishedListener(notifier)
            updater.addFinishedListener(this)
            updater.update()
        }

        private fun setRunSetupButtonHandler() {
            val button = findPreference<Preference>(getString(R.string.run_setup))
            button!!.setOnPreferenceClickListener {
                runSetup()
                return@setOnPreferenceClickListener true
            }
        }

        private fun runSetup() {
            val intent = Intent(this.requireActivity(), SetupActivity::class.java)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this.requireActivity()).toBundle())
        }

        override fun onConnectionAvailable() {
            requireActivity().runOnUiThread {
                updateNowPreference.isEnabled = true
                updateNowPreference.summary = getString(R.string.update_now_enabled_summary)
            }
        }

        override fun onConnectionLost() {
            requireActivity().runOnUiThread {
                updateNowPreference.isEnabled = false
                updateNowPreference.summary = getString(R.string.update_now_disabled_summary)
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onUpdateFinished(updated: Collection<Timetable.Info>) {
            val message = when (updated.isEmpty()) {
                true -> "All timetables are up-to-date"
                false -> "Updated ${updated.size} timetable(s)"
            }

            Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
        }

        private fun setVersionPreferenceSummary() {
            val versionPreference = findPreference<Preference>("version")
            versionPreference!!.summary = getVersion()
        }

        private fun getVersion(): String {
            val packageManager = requireContext().packageManager!!
            val packageInfo = packageManager.getPackageInfo(requireContext().packageName, 0)
            return packageInfo.versionName
        }

        private fun setRecentChangesClickHandler() {
            findPreference<Preference>("recent_changes")!!.setOnPreferenceClickListener {
                ChangeLog(requireContext()).run {
                    showRecent()
                }
                true
            }
        }

        override fun onDestroy() {
            connectivityCallback.cleanup()
            updateManager.setAlarm()
            super.onDestroy()
        }
    }
}