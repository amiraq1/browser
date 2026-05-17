package com.ammar.browser.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ammar.browser.R

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        supportActionBar?.apply {
            title = getString(R.string.about)
            setDisplayHomeAsUpEnabled(true)
        }

        findViewById<TextView>(R.id.about_version).text =
            getString(R.string.version_format, packageManager.getPackageInfo(packageName, 0).versionName)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
