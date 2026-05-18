package com.ammar.browser.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ammar.browser.R
import com.ammar.browser.performance.SpeedMode
import com.ammar.browser.performance.SpeedSettings
import com.ammar.browser.privacy.allowlist.SiteAllowlist
import com.ammar.browser.ui.AdBlockDebugActivity
import com.ammar.browser.ui.ProtectionStatsActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.apply { title = "Settings"; setDisplayHomeAsUpEnabled(true) }

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
            Toast.makeText(this, "Allowlist cleared", Toast.LENGTH_SHORT).show()
        }
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
    }

    private fun updateAllowlistCount() {
        val count = SiteAllowlist.getAll().size
        findViewById<TextView>(R.id.txt_allowlist_count).text = "$count sites allowlisted"
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
