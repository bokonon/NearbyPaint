package com.ys.nearbypaint.utiles

import android.content.Context
import android.preference.PreferenceManager

class SharedPreferencesManager {

    companion object {
        const val THICKNESS_KEY = "thickness"
        const val THICKNESS_DEFAULT_VALUE = 2
    }

    fun readThickness(context: Context): Int {
        return readInt(context, THICKNESS_KEY, THICKNESS_DEFAULT_VALUE)
    }

    fun putThickness(context: Context, value: Int) {
        putInt(context, THICKNESS_KEY, value)
    }

    private fun readInt(context: Context, key: String, defaultValue: Int): Int {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        return pref.getInt(key, defaultValue)
    }

    private fun putInt(context: Context, key: String, value: Int) {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = pref.edit()
        editor.putInt(key, value)
        editor.apply()
    }

}