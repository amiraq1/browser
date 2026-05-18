package com.ammar.browser.settings

import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ammar.browser.R
import com.ammar.browser.history.HistoryRepository
import com.ammar.browser.performance.SpeedMode
import com.ammar.browser.performance.SpeedSettings
import com.ammar.browser.privacy.CookieBannerSettings
import com.ammar.browser.privacy.allowlist.SiteAllowlist
import com.ammar.browser.search.SearchEngine
import com.ammar.browser.search.SearchSettings
import com.ammar.browser.ui.AdBlockDebugActivity
import com.ammar.browser.ui.AboutActivity
import com.ammar.browser.ui.ProtectionStatsActivity
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var historyRepository: HistoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.apply { title = "Settings"; setDisplayHomeAsUpEnabled(true) }

        historyRepository = HistoryRepository(this)

        updateSpeedModeDisplay()
        findViewById<Button>(R.id.btn_speed_off).setOnClickListener { setSpeed(SpeedMode.OFF) }
        findViewById<Button>(R.id.btn_speed_balanced).setOnClickListener { setSpeed(SpeedMode.BALANCED) }
        findViewById<Button>(R.id.btn_speed_extreme).setOnClickListener { setSpeed(SpeedMode.EXTREME) }

        findViewById<Button>(R.id.btn_protection_stats).setOnClickListener {
            startActivity(Intent(this, ProtectionStatsActivity::class.java))
        }
        findViewById<Button>(R.id.btn_adblock_debug).setOnClickListener {
            startActivity(Intent(this, AdBlockDebugActivity::class.java))
        }

        updateAllowlistCount()
        findViewById<Button>(R.id.btn_clear_allowlist).setOnClickListener {
            SiteAllowlist.clearAll()
            updateAllowlistCount()
            toast("Allowlist cleared")
        }

        findViewById<Button>(R.id.btn_clear_history).setOnClickListener {
            lifecycleScope.launch {
                historyRepository.clearAll()
                toast("History cleared")
            }
        }
        findViewById<Button>(R.id.btn_clear_cookies).setOnClickListener {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
            toast("Cookies cleared")
        }
        findViewById<Button>(R.id.btn_clear_cache).setOnClickListener {
            applicationContext.cacheDir.deleteRecursively()
            toast("Cache cleared")
        }
        findViewById<Button>(R.id.btn_clear_all).setOnClickListener {
            lifecycleScope.launch {
                historyRepository.clearAll()
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
                applicationContext.cacheDir.deleteRecursively()
                toast("All browsing data cleared")
            }
        }

        updateCookieBannerBtn()
        findViewById<Button>(R.id.btn_cookie_banner).setOnClickListener {
            CookieBannerSettings.setEnabled(!CookieBannerSettings.enabled)
            updateCookieBannerBtn()
        }

        updateSearchEngineDisplay()
        findViewById<Button>(R.id.btn_search_duckduckgo).setOnClickListener {
            setSearchEngine(SearchEngine.DUCKDUCKGO)
        }
        findViewById<Button>(R.id.btn_search_brave).setOnClickListener {
            setSearchEngine(SearchEngine.BRAVE)
        }
        findViewById<Button>(R.id.btn_search_startpage).setOnClickListener {
            setSearchEngine(SearchEngine.STARTPAGE)
        }
        findViewById<Button>(R.id.btn_search_google).setOnClickListener {
            setSearchEngine(SearchEngine.GOOGLE)
        }

        findViewById<Button>(R.id.btn_about).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }

    private fun updateCookieBannerBtn() {
        findViewById<Button>(R.id.btn_cookie_banner).text =
            "Cookie Banner Control: ${if (CookieBannerSettings.enabled) "Enabled" else "Disabled"}"
    }

    private fun setSpeed(mode: SpeedMode) {
        SpeedSettings.setMode(mode)
        updateSpeedModeDisplay()
    }

    private fun updateSpeedModeDisplay() {
        val mode = SpeedSettings.mode
        findViewById<TextView>(R.id.txt_speed_mode).text = "Current: ${mode.name}"
        findViewById<Button>(R.id.btn_speed_off).alpha = if (mode == SpeedMode.OFF) 1f else 0.5f
        findViewById<Button>(R.id.btn_speed_balanced).alpha = if (mode == SpeedMode.BALANCED) 1f else 0.5f
        findViewById<Button>(R.id.btn_speed_extreme).alpha = if (mode == SpeedMode.EXTREME) 1f else 0.5f
        val zt = findViewById<TextView>(R.id.txt_zero_tracking)
        if (mode == SpeedMode.EXTREME) {
            zt.text = "Zero Tracking Mode: Enabled"
            zt.setTextColor(0xFF4CAF50.toInt())
        } else {
            zt.text = "Zero Tracking Mode: Partial"
            zt.setTextColor(0xFFFF9800.toInt())
        }
    }

    private fun updateAllowlistCount() {
        val count = SiteAllowlist.getAll().size
        findViewById<TextView>(R.id.txt_allowlist_count).text = "$count sites allowlisted"
    }

    private fun setSearchEngine(engine: SearchEngine) {
        SearchSettings.setEngine(engine)
        updateSearchEngineDisplay()
        toast("Search engine: ${engine.displayName}")
    }

    private fun updateSearchEngineDisplay() {
        val current = SearchSettings.currentEngine
        findViewById<TextView>(R.id.txt_search_engine).text = "Current: ${current.displayName}"
        findViewById<Button>(R.id.btn_search_duckduckgo).alpha =
            if (current == SearchEngine.DUCKDUCKGO) 1f else 0.5f
        findViewById<Button>(R.id.btn_search_brave).alpha =
            if (current == SearchEngine.BRAVE) 1f else 0.5f
        findViewById<Button>(R.id.btn_search_startpage).alpha =
            if (current == SearchEngine.STARTPAGE) 1f else 0.5f
        findViewById<Button>(R.id.btn_search_google).alpha =
            if (current == SearchEngine.GOOGLE) 1f else 0.5f
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
