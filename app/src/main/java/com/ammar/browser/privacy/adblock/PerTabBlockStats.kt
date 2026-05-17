package com.ammar.browser.privacy.adblock

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class PerTabBlockStats {

    data class TabStats(
        val totalBlocked: AtomicInteger = AtomicInteger(0),
        val blockedAds: AtomicInteger = AtomicInteger(0),
        val blockedTrackers: AtomicInteger = AtomicInteger(0),
        val blockedAnalytics: AtomicInteger = AtomicInteger(0),
        val blockedSocial: AtomicInteger = AtomicInteger(0),
        val blockedSuspicious: AtomicInteger = AtomicInteger(0)
    )

    private val tabs = ConcurrentHashMap<String, TabStats>()

    fun record(tabId: String, decision: BlockDecision) {
        val s = tabs.getOrPut(tabId) { TabStats() }
        s.totalBlocked.incrementAndGet()
        when (decision) {
            BlockDecision.BLOCK_AD -> s.blockedAds.incrementAndGet()
            BlockDecision.BLOCK_TRACKER -> s.blockedTrackers.incrementAndGet()
            BlockDecision.BLOCK_ANALYTICS -> s.blockedAnalytics.incrementAndGet()
            BlockDecision.BLOCK_SOCIAL_TRACKER -> s.blockedSocial.incrementAndGet()
            BlockDecision.BLOCK_MALWARE_OR_SUSPICIOUS -> s.blockedSuspicious.incrementAndGet()
            else -> {}
        }
    }

    fun getTotalBlocked(tabId: String): Int = tabs[tabId]?.totalBlocked?.get() ?: 0

    fun getStats(tabId: String): TabStats? = tabs[tabId]

    fun removeTab(tabId: String) { tabs.remove(tabId) }

    fun getAll(): Map<String, TabStats> = tabs.toMap()
}
