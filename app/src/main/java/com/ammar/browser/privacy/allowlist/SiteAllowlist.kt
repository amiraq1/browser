package com.ammar.browser.privacy.allowlist

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri

/**
 * Per-site allowlist persisted via SharedPreferences.
 * Sites in this list bypass ad blocking entirely.
 */
object SiteAllowlist {

    private const val PREFS_NAME = "site_allowlist"
    private const val KEY_HOSTS = "allowed_hosts"

    private val allowedHosts = mutableSetOf<String>()
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        allowedHosts.clear()
        allowedHosts.addAll(prefs?.getStringSet(KEY_HOSTS, emptySet()) ?: emptySet())
    }

    fun addHost(host: String) {
        val normalized = normalizeHost(host) ?: return
        allowedHosts.add(normalized)
        persist()
    }

    fun removeHost(host: String) {
        val normalized = normalizeHost(host) ?: return
        allowedHosts.remove(normalized)
        persist()
    }

    fun isAllowed(pageUrl: String?): Boolean {
        val host = extractHost(pageUrl) ?: return false
        return isAllowedHost(host)
    }

    fun isAllowedHost(host: String): Boolean {
        val normalized = normalizeHost(host) ?: return false
        if (allowedHosts.contains(normalized)) return true
        for (allowed in allowedHosts) {
            if (normalized.endsWith(".$allowed")) return true
        }
        return false
    }

    fun getAll(): Set<String> = allowedHosts.toSet()

    private fun normalizeHost(host: String): String? {
        val lower = host.lowercase().trim()
        if (lower.isEmpty()) return null
        return lower.removePrefix("www.")
    }

    private fun extractHost(url: String?): String? {
        if (url.isNullOrBlank()) return null
        return try {
            Uri.parse(url).host?.lowercase()
        } catch (_: Exception) {
            null
        }
    }

    private fun persist() {
        prefs?.edit()?.putStringSet(KEY_HOSTS, allowedHosts.toSet())?.apply()
    }
}
