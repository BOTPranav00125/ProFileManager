package com.filemanager.pro.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getThemeMode(): String = prefs.getString(KEY_THEME, "system") ?: "system"

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME, mode).apply()
    }

    fun getViewMode(): String = prefs.getString(KEY_VIEW_MODE, "list") ?: "list"

    fun setViewMode(mode: String) {
        prefs.edit().putString(KEY_VIEW_MODE, mode).apply()
    }

    fun getSortBy(): String = prefs.getString(KEY_SORT_BY, "name") ?: "name"

    fun setSortBy(sortBy: String) {
        prefs.edit().putString(KEY_SORT_BY, sortBy).apply()
    }

    fun getSortOrder(): String = prefs.getString(KEY_SORT_ORDER, "asc") ?: "asc"

    fun setSortOrder(order: String) {
        prefs.edit().putString(KEY_SORT_ORDER, order).apply()
    }

    fun showHiddenFiles(): Boolean = prefs.getBoolean(KEY_SHOW_HIDDEN, false)

    fun setShowHiddenFiles(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_HIDDEN, show).apply()
    }

    fun getStartPath(): String = prefs.getString(KEY_START_PATH, "") ?: ""

    fun setStartPath(path: String) {
        prefs.edit().putString(KEY_START_PATH, path).apply()
    }

    companion object {
        private const val PREFS_NAME = "file_manager_prefs"
        private const val KEY_THEME = "theme"
        private const val KEY_VIEW_MODE = "view_mode"
        private const val KEY_SORT_BY = "sort_by"
        private const val KEY_SORT_ORDER = "sort_order"
        private const val KEY_SHOW_HIDDEN = "show_hidden"
        private const val KEY_START_PATH = "start_path"
    }
}