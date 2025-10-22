package com.filemanager.pro.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.filemanager.pro.FileManagerApplication
import com.filemanager.pro.R
import com.filemanager.pro.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarSettings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"

        binding.toolbarSettings.setNavigationOnClickListener {
            finish()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            val preferenceManager = (requireActivity().application as FileManagerApplication)
                .preferenceManager

            // Theme preference
            findPreference<ListPreference>("theme")?.setOnPreferenceChangeListener { _, newValue ->
                val themeMode = newValue as String
                preferenceManager.setThemeMode(themeMode)

                AppCompatDelegate.setDefaultNightMode(
                    when (themeMode) {
                        "light" -> AppCompatDelegate.MODE_NIGHT_NO
                        "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }
                )
                true
            }

            // View mode preference
            findPreference<ListPreference>("view_mode")?.setOnPreferenceChangeListener { _, newValue ->
                preferenceManager.setViewMode(newValue as String)
                true
            }

            // Sort by preference
            findPreference<ListPreference>("sort_by")?.setOnPreferenceChangeListener { _, newValue ->
                preferenceManager.setSortBy(newValue as String)
                true
            }

            // Show hidden files preference
            findPreference<SwitchPreferenceCompat>("show_hidden")?.setOnPreferenceChangeListener { _, newValue ->
                preferenceManager.setShowHiddenFiles(newValue as Boolean)
                true
            }
        }
    }
}