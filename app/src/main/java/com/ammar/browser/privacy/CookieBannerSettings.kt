package com.ammar.browser.privacy

import android.content.Context
import android.content.SharedPreferences

object CookieBannerSettings {
    private const val PREFS = "cookie_banner_prefs"
    private const val KEY = "enabled"
    private var prefs: SharedPreferences? = null
    var enabled: Boolean = true
        private set

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        enabled = prefs?.getBoolean(KEY, true) ?: true
    }

    fun setEnabled(value: Boolean) {
        enabled = value
        prefs?.edit()?.putBoolean(KEY, value)?.apply()
    }
}
