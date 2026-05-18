package com.ammar.browser.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ammar.browser.BrowserApp
import com.ammar.browser.R
import com.ammar.browser.performance.SpeedSettings
import com.ammar.browser.privacy.PrivacyGradeCalculator
import com.ammar.browser.privacy.TrackerCompanyClassifier

class ProtectionStatsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_protection_stats)

        val adBlocker = (application as BrowserApp).adBlocker
        val stats = adBlocker.stats
        val info = adBlocker.loadInfo
        val grade = PrivacyGradeCalculator.calculate(null, adBlocker, null)
        val companyCounts = TrackerCompanyClassifier.summarize(
            adBlocker.blockedLog.getRecent().map { it.host }
        )

        findViewById<TextView>(R.id.stat_privacy_grade).text = grade.toString()
        findViewById<TextView>(R.id.stat_total_blocked).text = stats.totalBlocked.toString()
        findViewById<TextView>(R.id.stat_ads).text = stats.blockedAds.toString()
        findViewById<TextView>(R.id.stat_trackers).text = stats.blockedTrackers.toString()
        findViewById<TextView>(R.id.stat_analytics).text = stats.blockedAnalytics.toString()
        findViewById<TextView>(R.id.stat_social).text = stats.blockedSocial.toString()
        findViewById<TextView>(R.id.stat_suspicious).text = stats.blockedSuspicious.toString()
        findViewById<TextView>(R.id.stat_speed_mode).text = SpeedSettings.mode.name
        findViewById<TextView>(R.id.stat_rules_loaded).text = "${info.totalRulesCount} rules (${info.abpExceptionsCount} exceptions)"
        findViewById<TextView>(R.id.stat_companies).text = companyCounts.entries
            .sortedByDescending { it.value }
            .joinToString("\n") { "${it.key}: ${it.value}" }
            .ifEmpty { "No data yet" }

        findViewById<Button>(R.id.btn_adblock_debug).setOnClickListener {
            startActivity(Intent(this, AdBlockDebugActivity::class.java))
        }
    }
}
