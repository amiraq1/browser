package com.ammar.browser.navigation

/**
 * Handles URL parsing and search query detection.
 */
object NavigationHelper {

    private const val DEFAULT_SEARCH_ENGINE = "https://www.google.com/search?q="

    fun resolveInput(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""

        return when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
            else -> DEFAULT_SEARCH_ENGINE + java.net.URLEncoder.encode(trimmed, "UTF-8")
        }
    }

    fun isUrl(input: String): Boolean {
        val trimmed = input.trim()
        return trimmed.startsWith("http://") ||
                trimmed.startsWith("https://") ||
                (trimmed.contains(".") && !trimmed.contains(" "))
    }
}
