package com.ammar.browser.privacy.adblock

import android.content.Context
import android.net.Uri
import android.util.Log
import com.ammar.browser.performance.SpeedMode
import com.ammar.browser.performance.SpeedSettings
import com.ammar.browser.privacy.allowlist.SiteAllowlist

/**
 * Core ad/tracker blocker with third-party detection.
 * Loads rules from assets (domain list + ABP subset); falls back to hardcoded defaults.
 */
class AdBlocker(context: Context? = null) {

    private val rules: List<BlockRule>
    private val domainIndex: Map<String, BlockDecision>
    private val exceptions: Set<String>
    val stats = BlockStats()
    val blockedLog = BlockedRequestLog()
    val perTabStats = PerTabBlockStats()
    val loadInfo: AdBlockLoadInfo
    private val currentMode: SpeedMode get() = SpeedSettings.mode

    companion object {
        private val SUSPICIOUS_KEYWORDS = listOf(
            "ads", "adservice", "analytics", "tracker", "pixel",
            "collect", "telemetry", "beacon"
        )
    }

    init {
        val loaded = loadAllRules(context)
        rules = loaded.rules
        exceptions = loaded.exceptions
        loadInfo = loaded.info
        domainIndex = buildDomainIndex()
        Log.d("AdBlocker", "Initialized: ${rules.size} rules, ${exceptions.size} exceptions")
    }

    fun shouldBlock(requestUrl: String, pageUrl: String?, tabId: String? = null): BlockDecision {
        if (currentMode == SpeedMode.OFF) return BlockDecision.ALLOW
        if (SiteAllowlist.isAllowed(pageUrl)) return BlockDecision.ALLOW

        stats.recordChecked()
        val requestHost = extractHost(requestUrl) ?: return BlockDecision.ALLOW

        if (isException(requestHost)) return BlockDecision.ALLOW

        val listDecision = matchDomain(requestHost, requestUrl)
        if (listDecision != BlockDecision.ALLOW) {
            recordBlock(listDecision, requestUrl, requestHost, pageUrl, tabId)
            return listDecision
        }

        if (currentMode == SpeedMode.EXTREME) {
            if (isThirdParty(requestHost, pageUrl) && hasSuspiciousKeyword(requestHost)) {
                val d = BlockDecision.BLOCK_MALWARE_OR_SUSPICIOUS
                recordBlock(d, requestUrl, requestHost, pageUrl, tabId)
                return d
            }
        }

        return BlockDecision.ALLOW
    }

    private fun recordBlock(decision: BlockDecision, url: String, host: String, pageUrl: String?, tabId: String?) {
        stats.record(decision)
        blockedLog.add(BlockedRequest(url, host, pageUrl, decision, tabId))
        tabId?.let { perTabStats.record(it, decision) }
    }

    private fun isException(requestHost: String): Boolean {
        if (exceptions.contains(requestHost)) return true
        for (ex in exceptions) {
            if (requestHost.endsWith(".$ex")) return true
        }
        return false
    }

    private data class LoadResult(
        val rules: List<BlockRule>,
        val exceptions: Set<String>,
        val info: AdBlockLoadInfo
    )

    private fun loadAllRules(context: Context?): LoadResult {
        if (context == null) {
            val fallback = DefaultBlockLists.buildRules()
            return LoadResult(fallback, emptySet(), AdBlockLoadInfo(fallback.size, 0, 0, fallback.size))
        }

        val domainRules = AssetBlockListProvider(context).getRules()
        val abpProvider = AbpAssetBlockListProvider(context)
        val abpRules = abpProvider.getRules()
        val allRules = domainRules + abpRules
        val finalRules = if (allRules.isEmpty()) DefaultBlockLists.buildRules() else allRules

        return LoadResult(
            finalRules,
            abpProvider.exceptions,
            AdBlockLoadInfo(domainRules.size, abpRules.size, abpProvider.exceptions.size, finalRules.size)
        )
    }

    private fun isThirdParty(requestHost: String, pageUrl: String?): Boolean {
        val pageHost = pageUrl?.let { extractHost(it) } ?: return false
        return approximateRegistrableDomain(requestHost) != approximateRegistrableDomain(pageHost)
    }

    private fun approximateRegistrableDomain(host: String): String {
        val parts = host.split(".")
        return if (parts.size >= 2) parts.takeLast(2).joinToString(".") else host
    }

    private fun hasSuspiciousKeyword(host: String): Boolean =
        SUSPICIOUS_KEYWORDS.any { host.contains(it) }

    private fun matchDomain(requestHost: String, requestUrl: String): BlockDecision {
        domainIndex[requestHost]?.let { return it }
        for ((domain, decision) in domainIndex) {
            if (requestHost.endsWith(".$domain")) return decision
        }
        for (rule in rules) {
            if (rule.domain.contains("/")) {
                val hostPart = rule.domain.substringBefore("/")
                val pathPart = "/" + rule.domain.substringAfter("/")
                if ((requestHost == hostPart || requestHost.endsWith(".$hostPart")) &&
                    requestUrl.contains(pathPart)
                ) return rule.decision
            }
        }
        return BlockDecision.ALLOW
    }

    private fun buildDomainIndex(): Map<String, BlockDecision> {
        val index = mutableMapOf<String, BlockDecision>()
        for (rule in rules) {
            if (!rule.domain.contains("/")) index[rule.domain] = rule.decision
        }
        return index
    }

    private fun extractHost(url: String): String? = try {
        Uri.parse(url).host?.lowercase()
    } catch (_: Exception) { null }
}
