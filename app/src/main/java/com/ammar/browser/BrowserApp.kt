package com.ammar.browser

import android.app.Application
import com.ammar.browser.performance.SpeedSettings
import com.ammar.browser.privacy.CookieBannerSettings
import com.ammar.browser.privacy.adblock.AdBlocker
import com.ammar.browser.privacy.allowlist.SiteAllowlist
import com.ammar.browser.utils.CrashLogger
import com.ammar.browser.utils.StartupTracker

class BrowserApp : Application() {

    lateinit var adBlocker: AdBlocker
        private set

    override fun onCreate() {
        super.onCreate()
        CrashLogger.install(this)
        StartupTracker.mark("CrashLogger installed")

        StartupTracker.mark("SiteAllowlist.init")
        SiteAllowlist.init(this)

        StartupTracker.mark("SpeedSettings.init")
        SpeedSettings.init(this)

        StartupTracker.mark("CookieBannerSettings.init")
        CookieBannerSettings.init(this)

        StartupTracker.mark("AdBlocker.init")
        adBlocker = AdBlocker(this)

        StartupTracker.mark("BrowserApp.onCreate complete")
    }

    companion object {
        fun getAdBlocker(app: Application): AdBlocker = (app as BrowserApp).adBlocker
    }
}
