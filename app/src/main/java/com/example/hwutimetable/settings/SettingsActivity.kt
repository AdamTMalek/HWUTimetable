package com.example.hwutimetable.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.hwutimetable.R

class SettingsActivity : AppCompatActivity() {

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

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onDisplayPreferenceDialog(preference: Preference?) {
            when (preference) {
                is TimePreference -> displayTimePreference(preference)
                is NumberPreference -> displayNumberPreference(preference)
                else -> super.onDisplayPreferenceDialog(preference)
            }
        }

        private fun displayTimePreference(preference: Preference) {
            TimePreferenceDialogFragmentCompat.newInstance(preference.key).let {
                it.setTargetFragment(this, 0)
                it.show(fragmentManager!!, "androidx.support.preference.PreferenceFragment.DIALOG")
            }
        }

        private fun displayNumberPreference(preference: Preference) {
            NumberPreferenceDialogFragmentCompat.newInstance(preference.key).let {
                it.setTargetFragment(this, 0)
                it.show(fragmentManager!!, "androidx.support.preference.PreferenceFragment.DIALOG")
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}