package com.example.browser

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.appbar.MaterialToolbar

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<MaterialToolbar>(R.id.settingsToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val adblockSwitch = findViewById<MaterialSwitch>(R.id.adblockSwitch)
        val rulesCountText = findViewById<TextView>(R.id.rulesCountText)
        val cleanAdsButton = findViewById<Button>(R.id.cleanAdsButton)
        val resetCounterButton = findViewById<Button>(R.id.resetCounterButton)

        val prefs = getSharedPreferences("browser_prefs", Context.MODE_PRIVATE)
        val adblockEnabled = prefs.getBoolean("adblock_enabled", true)
        val rulesCount = intent.getIntExtra("rules_count", 0)

        adblockSwitch.isChecked = adblockEnabled
        rulesCountText.text = getString(R.string.rules_loaded, rulesCount)

        adblockSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("adblock_enabled", isChecked).apply()
            setResult(RESULT_OK)
        }

        cleanAdsButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("action", "clean_ads")
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        resetCounterButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("action", "reset_counter")
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}
