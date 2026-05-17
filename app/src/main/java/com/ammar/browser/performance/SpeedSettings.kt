package com.ammar.browser.performance

import android.content.Context
import android.content.SharedPreferences

/**
 * Persisted speed/privacy mode. Changes take effect immediately on new requests.
 */
object SpeedSettings {

    private const val PREFS_NAME = "speed_settings"
    private const val KEY_MODE = "speed_mode"

    private var prefs: SharedPreferences? = null

    var mode: SpeedMode = SpeedMode.BALANCED
        private set

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs?.getString(KEY_MODE, null)
        mode = try {
            if (saved != null) SpeedMode.valueOf(saved) else SpeedMode.BALANCED
        } catch (_: Exception) {
            SpeedMode.BALANCED
        }
    }

    fun setMode(newMode: SpeedMode) {
        mode = newMode
        prefs?.edit()?.putString(KEY_MODE, newMode.name)?.apply()
    }
}
