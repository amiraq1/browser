package com.ammar.browser.performance

import android.content.Context
import android.content.SharedPreferences

/**
 * Persisted Lite Mode preference.
 *
 * When enabled, Nabd asks the WebView to skip automatic image loading
 * (`WebSettings.loadsImagesAutomatically = false`) so pages render
 * faster and use less data. JavaScript, DOM Storage, cookies, HTTPS
 * upgrade, AdBlock, and all other privacy/security defaults are
 * intentionally left untouched.
 *
 * Default: OFF.
 *
 * Mirrors the lifecycle of [SpeedSettings]: [init] is called once from
 * [com.ammar.browser.BrowserApp.onCreate], after which [enabled] is
 * safe to read from anywhere.
 */
object LiteModeSettings {

    private const val PREFS_NAME = "lite_mode_settings"
    private const val KEY_ENABLED = "enabled"

    private var prefs: SharedPreferences? = null

    var enabled: Boolean = false
        private set

    /**
     * Optional listener used by [com.ammar.browser.ui.MainActivity] to
     * push the new value into all live WebViews so the change takes
     * effect on the current tab without an app restart.
     *
     * Kept deliberately minimal: a single callback, not a list, because
     * the only consumer is the foreground Activity. Cleared in
     * `onDestroy` to avoid Activity leaks.
     */
    var onChange: ((Boolean) -> Unit)? = null

    fun init(context: Context) {
        prefs = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        enabled = prefs?.getBoolean(KEY_ENABLED, false) ?: false
    }

    fun setEnabled(value: Boolean) {
        enabled = value
        prefs?.edit()?.putBoolean(KEY_ENABLED, value)?.apply()
        onChange?.invoke(value)
    }
}
