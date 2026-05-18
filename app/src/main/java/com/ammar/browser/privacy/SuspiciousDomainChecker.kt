package com.ammar.browser.privacy

import android.net.Uri

/**
 * Local-only suspicious domain checker. No network lookups.
 */
object SuspiciousDomainChecker {

    private val suspiciousDomains = setOf(
        "popads.net", "popcash.net", "clickadu.com", "propellerads.com",
        "adsterra.com", "hilltopads.com", "trafficjunky.com", "exoclick.com",
        "juicyads.com", "trafficfactory.biz", "plugrush.com", "adcash.com"
    )

    private val suspiciousKeywords = listOf(
        "malware", "phishing", "scam", "popads", "clickadu",
        "propellerads", "adsterra"
    )

    private val skipPrefixes = listOf("about:", "ammar:", "data:", "javascript:", "file:", "content:")

    fun isSuspicious(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        if (skipPrefixes.any { url.startsWith(it) }) return false
        val host = extractHost(url) ?: return false
        if (suspiciousDomains.any { host == it || host.endsWith(".$it") }) return true
        if (suspiciousKeywords.any { host.contains(it) }) return true
        return false
    }

    fun getReason(url: String?): String {
        val host = extractHost(url) ?: return "Unknown risk"
        if (suspiciousDomains.any { host == it || host.endsWith(".$it") })
            return "Known aggressive ad/tracking network"
        if (suspiciousKeywords.any { host.contains(it) })
            return "Domain name contains suspicious keywords"
        return "Unknown risk"
    }

    private fun extractHost(url: String?): String? = try {
        Uri.parse(url).host?.lowercase()
    } catch (_: Exception) { null }
}
