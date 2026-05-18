package com.ammar.browser.ui

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ammar.browser.R
import com.ammar.browser.bookmarks.BookmarkRepository
import com.ammar.browser.engine.BrowserEngine
import com.ammar.browser.engine.EngineCallback
import com.ammar.browser.engine.WebViewEngine
import com.ammar.browser.history.HistoryRepository
import com.ammar.browser.navigation.NavigationHelper
import com.ammar.browser.performance.SpeedMode
import com.ammar.browser.performance.SpeedSettings
import com.ammar.browser.privacy.PrivacyGradeCalculator
import com.ammar.browser.privacy.SuspiciousDomainChecker
import com.ammar.browser.privacy.TrackerCompanyClassifier
import com.ammar.browser.privacy.adblock.AdBlocker
import com.ammar.browser.privacy.allowlist.SiteAllowlist
import com.ammar.browser.settings.SettingsActivity
import com.ammar.browser.tabs.Tab
import com.ammar.browser.tabs.TabManager
import com.ammar.browser.utils.StartupTracker
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), EngineCallback, TabManager.Listener {

    private val tabManager = TabManager()
    private val engines = mutableMapOf<String, BrowserEngine>()
    private lateinit var adBlocker: AdBlocker
    private lateinit var historyRepository: HistoryRepository
    private lateinit var bookmarkRepository: BookmarkRepository

    private lateinit var urlBar: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageButton
    private lateinit var btnForward: ImageButton
    private lateinit var btnRefresh: ImageButton
    private lateinit var btnMenu: ImageButton
    private lateinit var btnTabs: TextView
    private lateinit var blockedCount: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var engineContainer: FrameLayout

    private val tabSwitcherLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val action = result.data?.getStringExtra(TabSwitcherActivity.RESULT_ACTION) ?: return@registerForActivityResult
            val tabId = result.data?.getStringExtra(TabSwitcherActivity.RESULT_TAB_ID) ?: ""
            when (action) {
                TabSwitcherActivity.ACTION_SELECT -> tabManager.selectTab(tabId)
                TabSwitcherActivity.ACTION_CLOSE -> tabManager.closeTab(tabId)
                TabSwitcherActivity.ACTION_NEW -> tabManager.createNewTab(NewTabPage.URL)
            }
        }
    }

    private val historyLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val url = result.data?.getStringExtra(HistoryActivity.RESULT_URL) ?: return@registerForActivityResult
            navigateTo(url)
        }
    }

    private val bookmarksLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val url = result.data?.getStringExtra(BookmarksActivity.RESULT_URL) ?: return@registerForActivityResult
            navigateTo(url)
        }
    }

    // --- Lifecycle ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StartupTracker.mark("MainActivity.setContentView")
        setContentView(R.layout.activity_main)
        StartupTracker.mark("MainActivity.initViews")
        initViews()
        StartupTracker.mark("MainActivity.adBlocker")
        adBlocker = (application as com.ammar.browser.BrowserApp).adBlocker
        StartupTracker.mark("MainActivity.historyRepository")
        historyRepository = HistoryRepository(this)
        bookmarkRepository = BookmarkRepository(this)
        StartupTracker.mark("MainActivity.tabManager")
        tabManager.setListener(this)
        tabManager.createNewTab(intent?.dataString ?: NewTabPage.URL)
        StartupTracker.mark("MainActivity.onCreate complete")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.dataString?.let { tabManager.createNewTab(it) }
    }

    override fun onDestroy() {
        engines.values.forEach { it.destroy() }
        engines.clear()
        super.onDestroy()
    }

    @Deprecated("Use OnBackPressedDispatcher")
    override fun onBackPressed() {
        if (currentEngine()?.canGoBack() == true) {
            currentEngine()?.goBack()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

    // --- Init ---

    private fun initViews() {
        urlBar = findViewById(R.id.url_bar)
        progressBar = findViewById(R.id.progress_bar)
        btnBack = findViewById(R.id.btn_back)
        btnForward = findViewById(R.id.btn_forward)
        btnRefresh = findViewById(R.id.btn_refresh)
        btnMenu = findViewById(R.id.btn_menu)
        btnTabs = findViewById(R.id.btn_tabs)
        blockedCount = findViewById(R.id.blocked_count)
        swipeRefresh = findViewById(R.id.swipe_refresh)
        engineContainer = findViewById(R.id.engine_container)

        urlBar.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                navigateTo(urlBar.text.toString())
                urlBar.clearFocus()
                true
            } else false
        }

        btnBack.setOnClickListener { currentEngine()?.goBack() }
        btnForward.setOnClickListener { currentEngine()?.goForward() }
        btnRefresh.setOnClickListener { currentEngine()?.reload() }
        btnMenu.setOnClickListener { showMainMenu(it) }
        btnTabs.setOnClickListener { openTabSwitcher() }
        blockedCount.setOnClickListener { showProtectionPanel() }
        swipeRefresh.setOnRefreshListener { currentEngine()?.reload() }
    }

    // --- Helpers ---

    private fun isActiveTab(tabId: String): Boolean =
        tabManager.getCurrentTab()?.id == tabId

    private fun currentEngine(): BrowserEngine? =
        tabManager.getCurrentTab()?.id?.let { engines[it] }

    private fun createEngineForTab(tabId: String): BrowserEngine {
        val engine = WebViewEngine(tabId, adBlocker)
        engine.setCallback(this)
        engine.initialize(this)
        engine.createEngineView(this)
        engines[tabId] = engine
        return engine
    }

    private fun showEngineForTab(tabId: String) {
        engineContainer.removeAllViews()
        val engine = engines[tabId] ?: createEngineForTab(tabId)
        val view = engine.getView() ?: engine.createEngineView(this)
        (view.parent as? android.view.ViewGroup)?.removeView(view)
        engineContainer.addView(view)
        updateUiForCurrentTab()
    }

    /** Syncs all UI elements to reflect the current active tab's state. */
    private fun updateUiForCurrentTab() {
        val tab = tabManager.getCurrentTab() ?: return
        val engine = engines[tab.id]

        val currentUrl = engine?.getCurrentUrl() ?: tab.url
        urlBar.setText(if (NewTabPage.isNewTabUrl(currentUrl)) "" else currentUrl)
        btnBack.alpha = if (engine?.canGoBack() == true) 1.0f else 0.4f
        btnForward.alpha = if (engine?.canGoForward() == true) 1.0f else 0.4f
        btnTabs.text = tabManager.getTabCount().toString()
        progressBar.visibility = if (tab.isLoading) View.VISIBLE else View.GONE
        swipeRefresh.isRefreshing = false
        updateBlockedCount()
    }

    private fun updateBlockedCount() {
        val tabId = tabManager.getCurrentTab()?.id
        val tabBlocked = tabId?.let { adBlocker.perTabStats.getTotalBlocked(it) } ?: 0
        val total = adBlocker.stats.totalBlocked
        val mode = SpeedSettings.mode.name
        val currentUrl = currentEngine()?.getCurrentUrl()
        val siteAllowed = SiteAllowlist.isAllowed(currentUrl)
        if (total > 0 || SpeedSettings.mode != SpeedMode.OFF || siteAllowed) {
            blockedCount.text = if (siteAllowed) "\uD83D\uDEE1 Allowed | $mode" else "\uD83D\uDEE1 $tabBlocked blocked | $mode"
            blockedCount.visibility = View.VISIBLE
        } else {
            blockedCount.visibility = View.GONE
        }
    }

    private fun showProtectionPanel() {
        val dialog = BottomSheetDialog(this)
        val currentUrl = currentEngine()?.getCurrentUrl()
        val host = currentUrl?.let { android.net.Uri.parse(it).host } ?: "—"
        val siteAllowed = SiteAllowlist.isAllowed(currentUrl)
        val tabId = tabManager.getCurrentTab()?.id
        val tabBlocked = tabId?.let { adBlocker.perTabStats.getTotalBlocked(it) } ?: 0
        val totalBlocked = adBlocker.stats.totalBlocked
        val mode = SpeedSettings.mode
        val zeroTracking = if (mode == SpeedMode.EXTREME) "Enabled" else "Partial"

        val pad = (16 * resources.displayMetrics.density).toInt()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        val grade = PrivacyGradeCalculator.calculate(currentUrl, adBlocker, tabId)

        layout.addView(TextView(this).apply {
            text = "\uD83D\uDEE1 $host"
            textSize = 16f
            setPadding(0, 0, 0, pad / 2)
        })

        val recentHosts = adBlocker.blockedLog.getRecent().map { it.host }
        val topCompany = TrackerCompanyClassifier.topCompany(recentHosts)

        layout.addView(TextView(this).apply {
            text = "Privacy Grade: $grade\n" +
                    "Protection: ${if (siteAllowed) "Allowed" else "Protected"}\n" +
                    "Zero Tracking: $zeroTracking\n" +
                    "Blocked: $tabBlocked on this tab\n" +
                    "Top tracker: $topCompany\n" +
                    "HTTPS-Only: Enabled\n" +
                    "Cookie banners: ${if (com.ammar.browser.privacy.CookieBannerSettings.enabled) "Hidden" else "Off"}"
            textSize = 13f
            setLineSpacing(4f, 1f)
            setPadding(0, 0, 0, pad)
        })

        fun speedBtn(label: String, m: SpeedMode) = Button(this).apply {
            text = label + if (mode == m) " ✓" else ""
            setOnClickListener { SpeedSettings.setMode(m); updateBlockedCount(); dialog.dismiss() }
        }
        layout.addView(speedBtn("Speed OFF", SpeedMode.OFF))
        layout.addView(speedBtn("Speed BALANCED", SpeedMode.BALANCED))
        layout.addView(speedBtn("Speed EXTREME", SpeedMode.EXTREME))

        layout.addView(Button(this).apply {
            text = if (siteAllowed) "Enable protection for this site" else "Disable protection for this site"
            setOnClickListener { toggleAllowlistForCurrentSite(); dialog.dismiss() }
        })

        layout.addView(Button(this).apply {
            text = "Clear cookies for this site"
            setOnClickListener {
                val cm = android.webkit.CookieManager.getInstance()
                val siteCookies = cm.getCookie(currentUrl ?: "")
                if (siteCookies != null && host != "—") {
                    // Remove each cookie by setting expired value
                    siteCookies.split(";").forEach { cookie ->
                        val name = cookie.split("=").firstOrNull()?.trim() ?: return@forEach
                        cm.setCookie(host, "$name=; Expires=Thu, 01 Jan 1970 00:00:00 GMT")
                    }
                    cm.flush()
                    android.widget.Toast.makeText(this@MainActivity, "Cookies cleared for $host", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(this@MainActivity, "No cookies for this site", android.widget.Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
        })

        layout.addView(Button(this).apply {
            text = "Open AdBlock Debug"
            setOnClickListener {
                startActivity(Intent(this@MainActivity, AdBlockDebugActivity::class.java))
                dialog.dismiss()
            }
        })

        layout.addView(Button(this).apply {
            text = "Protection Stats"
            setOnClickListener {
                startActivity(Intent(this@MainActivity, ProtectionStatsActivity::class.java))
                dialog.dismiss()
            }
        })

        dialog.setContentView(layout)
        dialog.show()
    }

    private fun toggleAllowlistForCurrentSite() {
        val url = currentEngine()?.getCurrentUrl() ?: return
        val host = android.net.Uri.parse(url).host ?: return
        if (SiteAllowlist.isAllowedHost(host)) {
            SiteAllowlist.removeHost(host)
        } else {
            SiteAllowlist.addHost(host)
        }
        currentEngine()?.reload()
        updateBlockedCount()
    }

    private fun navigateTo(input: String) {
        val url = NavigationHelper.resolveInput(input)
        if (url.isEmpty()) return
        if (SuspiciousDomainChecker.isSuspicious(url)) {
            showSuspiciousWarning(url)
        } else {
            currentEngine()?.loadUrl(url)
        }
    }

    private fun showSuspiciousWarning(url: String) {
        val reason = SuspiciousDomainChecker.getReason(url)
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("⚠️ This site may be unsafe")
            .setMessage("$url\n\n$reason")
            .setNegativeButton("Go Back", null)
            .setPositiveButton("Continue Anyway") { _, _ -> currentEngine()?.loadUrl(url) }
            .show()
    }

    /**
     * Saves the currently displayed page as a bookmark.
     *
     * Skipped (with a toast) when:
     *  - The active tab is private.
     *  - There is no current URL, or it is the new-tab page / blank / data URI.
     *  - The URL is not http(s) (the repository enforces this too).
     *
     * If a bookmark for this URL already exists, its title and timestamp are
     * refreshed instead of inserting a duplicate row (unique index on `url`).
     */
    private fun addCurrentPageAsBookmark() {
        val tab = tabManager.getCurrentTab()
        if (tab == null) {
            android.widget.Toast
                .makeText(this, R.string.bookmark_skipped_invalid, android.widget.Toast.LENGTH_SHORT)
                .show()
            return
        }
        if (tab.isPrivate) {
            android.widget.Toast
                .makeText(this, R.string.bookmark_skipped_private, android.widget.Toast.LENGTH_SHORT)
                .show()
            return
        }
        val engine = engines[tab.id]
        val url = engine?.getCurrentUrl() ?: tab.url
        if (url.isBlank() || NewTabPage.isNewTabUrl(url) ||
            !(url.startsWith("http://") || url.startsWith("https://"))
        ) {
            android.widget.Toast
                .makeText(this, R.string.bookmark_skipped_invalid, android.widget.Toast.LENGTH_SHORT)
                .show()
            return
        }
        val title = engine?.getTitle()?.takeIf { it.isNotBlank() } ?: tab.title
        lifecycleScope.launch {
            val result = bookmarkRepository.addOrUpdate(url, title)
            val msgRes = when (result) {
                BookmarkRepository.AddResult.ADDED -> R.string.bookmark_added
                BookmarkRepository.AddResult.UPDATED -> R.string.bookmark_updated
                BookmarkRepository.AddResult.SKIPPED -> R.string.bookmark_skipped_invalid
            }
            runOnUiThread {
                android.widget.Toast
                    .makeText(this@MainActivity, msgRes, android.widget.Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun showMainMenu(anchor: View) {
        PopupMenu(this, anchor).apply {
            menuInflater.inflate(R.menu.main_menu, menu)
            // Mark current speed mode with ✓
            val currentMode = SpeedSettings.mode
            menu.findItem(R.id.menu_speed_off).title = "Speed: OFF" + if (currentMode == SpeedMode.OFF) " ✓" else ""
            menu.findItem(R.id.menu_speed_balanced).title = "Speed: BALANCED" + if (currentMode == SpeedMode.BALANCED) " ✓" else ""
            menu.findItem(R.id.menu_speed_extreme).title = "Speed: EXTREME" + if (currentMode == SpeedMode.EXTREME) " ✓" else ""

            // Allowlist toggle for current site
            val currentUrl = currentEngine()?.getCurrentUrl()
            val siteAllowed = SiteAllowlist.isAllowed(currentUrl)
            menu.findItem(R.id.menu_toggle_allowlist).title =
                if (siteAllowed) getString(R.string.enable_blocking_site)
                else getString(R.string.disable_blocking_site)

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_toggle_allowlist -> { toggleAllowlistForCurrentSite(); true }
                    R.id.menu_speed_off -> { SpeedSettings.setMode(SpeedMode.OFF); updateBlockedCount(); true }
                    R.id.menu_speed_balanced -> { SpeedSettings.setMode(SpeedMode.BALANCED); updateBlockedCount(); true }
                    R.id.menu_speed_extreme -> { SpeedSettings.setMode(SpeedMode.EXTREME); updateBlockedCount(); true }
                    R.id.menu_adblock_debug -> {
                        startActivity(Intent(this@MainActivity, AdBlockDebugActivity::class.java))
                        true
                    }
                    R.id.menu_history -> {
                        historyLauncher.launch(Intent(this@MainActivity, HistoryActivity::class.java))
                        true
                    }
                    R.id.menu_add_bookmark -> {
                        addCurrentPageAsBookmark()
                        true
                    }
                    R.id.menu_bookmarks -> {
                        bookmarksLauncher.launch(Intent(this@MainActivity, BookmarksActivity::class.java))
                        true
                    }
                    R.id.menu_settings -> {
                        startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun openTabSwitcher() {
        val tabs = tabManager.getAllTabs()
        val jsonArray = org.json.JSONArray()
        tabs.forEach { tab ->
            jsonArray.put(org.json.JSONObject().apply {
                put("id", tab.id)
                put("title", tab.title)
                put("url", tab.url)
            })
        }
        val intent = Intent(this, TabSwitcherActivity::class.java).apply {
            putExtra(TabSwitcherActivity.EXTRA_TABS, jsonArray.toString())
            putExtra(TabSwitcherActivity.EXTRA_ACTIVE_TAB_ID, tabManager.getCurrentTab()?.id)
        }
        tabSwitcherLauncher.launch(intent)
    }

    // --- TabManager.Listener ---

    override fun onTabCreated(tab: Tab) {
        val engine = createEngineForTab(tab.id)
        showEngineForTab(tab.id)
        if (NewTabPage.isNewTabUrl(tab.url)) {
            engine.loadHtml(NewTabPage.generateHtml(adBlocker), NewTabPage.URL)
            urlBar.setText("")
        } else if (tab.url.isNotEmpty()) {
            engine.loadUrl(tab.url)
        }
    }

    override fun onTabClosed(tabId: String, newActiveTab: Tab?) {
        engines[tabId]?.destroy()
        engines.remove(tabId)
        adBlocker.perTabStats.removeTab(tabId)
        newActiveTab?.let { showEngineForTab(it.id) }
        btnTabs.text = tabManager.getTabCount().toString()
    }

    override fun onTabSelected(tab: Tab) {
        showEngineForTab(tab.id)
    }

    override fun onTabUpdated(tab: Tab) {
        // UI already updated in EngineCallback if active
    }

    // --- EngineCallback (multi-tab aware) ---

    override fun onPageStarted(tabId: String, url: String) {
        runOnUiThread {
            // Always update tab data
            tabManager.updateTabUrl(tabId, url)
            tabManager.updateLoadingState(tabId, true)

            // Only update UI if this is the active tab
            if (isActiveTab(tabId)) {
                urlBar.setText(if (NewTabPage.isNewTabUrl(url)) "" else url)
                progressBar.visibility = View.VISIBLE
            }
        }
    }

    override fun onPageFinished(tabId: String, url: String) {
        runOnUiThread {
            // Always update tab data
            tabManager.updateLoadingState(tabId, false)
            val engine = engines[tabId]
            tabManager.updateNavState(
                tabId,
                engine?.canGoBack() == true,
                engine?.canGoForward() == true
            )

            // Only update UI if this is the active tab
            if (isActiveTab(tabId)) {
                progressBar.visibility = View.GONE
                swipeRefresh.isRefreshing = false
                btnBack.alpha = if (engine?.canGoBack() == true) 1.0f else 0.4f
                btnForward.alpha = if (engine?.canGoForward() == true) 1.0f else 0.4f
                updateBlockedCount()
            }

            // Record history (skip private tabs and new tab page)
            val tab = tabManager.getAllTabs().find { it.id == tabId }
            if (tab != null && !tab.isPrivate && !NewTabPage.isNewTabUrl(url)) {
                val title = engine?.getTitle() ?: tab.title
                lifecycleScope.launch { historyRepository.recordVisit(url, title) }
            }
        }
    }

    override fun onProgressChanged(tabId: String, progress: Int) {
        runOnUiThread {
            if (isActiveTab(tabId)) {
                progressBar.progress = progress
                if (progress == 100) progressBar.visibility = View.GONE
            }
        }
    }

    override fun onTitleChanged(tabId: String, title: String?) {
        runOnUiThread {
            tabManager.updateTabTitle(tabId, title ?: "")
        }
    }

    /**
     * Handles `ammar://action/<name>` links fired from the local
     * Shield Dashboard. The engine guarantees these only originate from
     * an `ammar://` page (origin gate in [WebViewEngine]), so external
     * sites cannot trigger native actions even via redirects.
     *
     * Unknown actions are ignored silently.
     */
    override fun onCustomAction(tabId: String, action: String) {
        runOnUiThread {
            when (action) {
                "protection-stats" -> {
                    startActivity(Intent(this, ProtectionStatsActivity::class.java))
                }
                "settings" -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
                "clear-data" -> {
                    // Settings hosts the Clear Browsing Data buttons.
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
                "extreme-mode" -> {
                    SpeedSettings.setMode(SpeedMode.EXTREME)
                    android.widget.Toast.makeText(
                        this,
                        "Extreme Mode enabled",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    updateBlockedCount()
                    // If we're on the new tab page, regenerate it so the
                    // dashboard immediately reflects the new mode.
                    val engine = engines[tabId]
                    val currentUrl = engine?.getCurrentUrl()
                    if (engine != null && NewTabPage.isNewTabUrl(currentUrl)) {
                        engine.loadHtml(
                            NewTabPage.generateHtml(adBlocker),
                            NewTabPage.URL
                        )
                    }
                }
                // Unknown action: ignore.
            }
        }
    }
}
