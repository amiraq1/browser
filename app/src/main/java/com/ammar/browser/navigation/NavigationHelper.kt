package com.ammar.browser.navigation

import com.ammar.browser.search.SearchSettings

/**
 * Handles URL parsing and search query detection.
 *
 * Search queries are routed through the user's selected engine (see
 * [com.ammar.browser.search.SearchSettings]); the default is DuckDuckGo
 * to match the Zero Tracking identity.
 */
object NavigationHelper {

    private val SKIP_SCHEMES = listOf("about:", "ammar:", "data:", "javascript:", "file:", "content:")

    var httpsUpgradeCount: Int = 0
        private set

    fun resolveInput(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""

        val url = when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
            else -> SearchSettings.currentEngine.searchUrl(trimmed)
        }
        return upgradeToHttps(url)
    }

    /** Upgrades http:// to https:// unless it's a special scheme. */
    fun upgradeToHttps(url: String): String {
        if (SKIP_SCHEMES.any { url.startsWith(it) }) return url
        if (url.startsWith("http://")) {
            httpsUpgradeCount++
            return "https://" + url.removePrefix("http://")
        }
        return url
    }

    fun isUrl(input: String): Boolean {
        val trimmed = input.trim()
        return trimmed.startsWith("http://") ||
                trimmed.startsWith("https://") ||
                (trimmed.contains(".") && !trimmed.contains(" "))
    }
}
