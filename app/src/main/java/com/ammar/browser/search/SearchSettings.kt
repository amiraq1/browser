package com.ammar.browser.search

import android.content.Context
import android.content.SharedPreferences

/**
 * Persisted search-engine preference. Defaults to [SearchEngine.DUCKDUCKGO]
 * to align with the Zero Tracking identity of Nabd Browser.
 *
 * Mirrors the lifecycle of [com.ammar.browser.performance.SpeedSettings]:
 * [init] is called once from [com.ammar.browser.BrowserApp.onCreate], after
 * which [currentEngine] is safe to read from anywhere.
 */
object SearchSettings {

    private const val PREFS_NAME = "search_settings"
    private const val KEY_ENGINE = "engine_id"

    private var prefs: SharedPreferences? = null

    var currentEngine: SearchEngine = SearchEngine.DUCKDUCKGO
        private set

    fun init(context: Context) {
        prefs = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs?.getString(KEY_ENGINE, null)
        currentEngine = SearchEngine.fromId(saved)
    }

    fun setEngine(engine: SearchEngine) {
        currentEngine = engine
        prefs?.edit()?.putString(KEY_ENGINE, engine.id)?.apply()
    }
}
