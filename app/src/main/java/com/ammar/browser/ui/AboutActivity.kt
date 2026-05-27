package com.ammar.browser.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ammar.browser.R
import com.ammar.browser.utils.applySystemBarPaddingToContent

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        applySystemBarPaddingToContent()
        supportActionBar?.apply { title = "About"; setDisplayHomeAsUpEnabled(true) }

        findViewById<TextView>(R.id.about_version).text =
            "Version ${packageManager.getPackageInfo(packageName, 0).versionName}"
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
