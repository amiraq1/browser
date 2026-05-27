package com.ammar.browser.ui

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ammar.browser.BrowserApp
import com.ammar.browser.R
import com.ammar.browser.performance.SpeedSettings
import com.ammar.browser.utils.applySystemBarPaddingToContent

class AdBlockDebugActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adblock_debug)
        applySystemBarPaddingToContent()

        val adBlocker = (application as BrowserApp).adBlocker
        val stats = adBlocker.stats
        val info = adBlocker.loadInfo

        val statsText = buildString {
            appendLine("Speed Mode: ${SpeedSettings.mode}")
            appendLine()
            appendLine("── Global Stats ──")
            appendLine("Requests checked: ${stats.totalRequestsChecked}")
            appendLine("Total blocked:    ${stats.totalBlocked}")
            appendLine("  Ads:            ${stats.blockedAds}")
            appendLine("  Trackers:       ${stats.blockedTrackers}")
            appendLine("  Analytics:      ${stats.blockedAnalytics}")
            appendLine("  Social:         ${stats.blockedSocial}")
            appendLine("  Suspicious:     ${stats.blockedSuspicious}")
            appendLine()
            appendLine("── Per-Tab Stats ──")
            val tabStats = adBlocker.perTabStats.getAll()
            if (tabStats.isEmpty()) {
                appendLine("No active tabs")
            } else {
                tabStats.forEach { (tabId, ts) ->
                    appendLine("Tab ${tabId.take(8)}: ${ts.totalBlocked.get()} blocked")
                }
            }
            appendLine()
            appendLine("── Rules Loaded ──")
            appendLine("Domain list:      ${info.defaultDomainRulesCount}")
            appendLine("ABP rules:        ${info.abpRulesCount}")
            appendLine("ABP exceptions:   ${info.abpExceptionsCount}")
            appendLine("Total rules:      ${info.totalRulesCount}")
        }

        findViewById<TextView>(R.id.debug_stats).text = statsText

        val container = findViewById<LinearLayout>(R.id.blocked_list)
        val recent = adBlocker.blockedLog.getRecent()

        if (recent.isEmpty()) {
            val tv = TextView(this)
            tv.text = "No blocked requests yet"
            tv.textSize = 13f
            tv.setTextColor(getColor(R.color.nabd_text_muted))
            container.addView(tv)
        } else {
            recent.forEach { req ->
                val tv = TextView(this)
                tv.textSize = 12f
                tv.setPadding(0, 4, 0, 4)
                tv.setTextColor(getColor(R.color.nabd_text_secondary))
                tv.text = "${req.decision.name}  ${req.host}"
                container.addView(tv)
            }
        }
    }
}
