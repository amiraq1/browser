package com.ammar.browser.privacy

import com.ammar.browser.performance.SpeedMode
import com.ammar.browser.performance.SpeedSettings
import com.ammar.browser.privacy.adblock.AdBlocker
import com.ammar.browser.privacy.allowlist.SiteAllowlist

/**
 * Calculates a simple privacy grade (A-D) based on current protection state.
 */
object PrivacyGradeCalculator {

    fun calculate(pageUrl: String?, adBlocker: AdBlocker, tabId: String?): Char {
        val mode = SpeedSettings.mode
        val siteAllowed = SiteAllowlist.isAllowed(pageUrl)
        val tabBlocked = tabId?.let { adBlocker.perTabStats.getTotalBlocked(it) } ?: 0

        return when {
            siteAllowed || mode == SpeedMode.OFF -> 'D'
            mode == SpeedMode.EXTREME && tabBlocked > 0 -> 'A'
            mode == SpeedMode.EXTREME -> 'B'
            mode == SpeedMode.BALANCED && tabBlocked > 0 -> 'B'
            else -> 'C'
        }
    }
}
