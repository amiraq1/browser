package com.ammar.browser.navigation

/**
 * Handles URL parsing and search query detection.
 */
object NavigationHelper {

    private const val DEFAULT_SEARCH_ENGINE = "https://www.google.com/search?q="
    private val SKIP_SCHEMES = listOf("about:", "ammar:", "data:", "javascript:", "file:", "content:")

    var httpsUpgradeCount: Int = 0
        private set

    fun resolveInput(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""

        val url = when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
            else -> DEFAULT_SEARCH_ENGINE + java.net.URLEncoder.encode(trimmed, "UTF-8")
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
