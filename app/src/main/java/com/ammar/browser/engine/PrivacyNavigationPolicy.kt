package com.ammar.browser.engine

import com.ammar.browser.navigation.NavigationHelper

/**
 * Centralizes top-level navigation policy for privacy-sensitive WebView loads.
 */
object PrivacyNavigationPolicy {
    val PRIVACY_HEADERS: Map<String, String> = mapOf(
        "DNT" to "1",
        "Sec-GPC" to "1"
    )

    fun shouldHandleAsTopLevelWebNavigation(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }

    fun secureTopLevelUrl(url: String): String {
        return NavigationHelper.upgradeToHttps(url)
    }

    fun localActionName(url: String): String? {
        if (!url.startsWith("ammar://action/")) return null
        return url
            .removePrefix("ammar://action/")
            .trimEnd('/')
            .takeIf { it.isNotEmpty() }
    }
}
