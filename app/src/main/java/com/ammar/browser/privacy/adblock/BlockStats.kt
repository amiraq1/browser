package com.ammar.browser.privacy.adblock

import java.util.concurrent.atomic.AtomicInteger

class BlockStats {
    private val _totalRequestsChecked = AtomicInteger(0)
    private val _totalBlocked = AtomicInteger(0)
    private val _blockedAds = AtomicInteger(0)
    private val _blockedTrackers = AtomicInteger(0)
    private val _blockedAnalytics = AtomicInteger(0)
    private val _blockedSocial = AtomicInteger(0)
    private val _blockedSuspicious = AtomicInteger(0)
    private val _blockedUnknown = AtomicInteger(0)

    val totalRequestsChecked: Int get() = _totalRequestsChecked.get()
    val totalBlocked: Int get() = _totalBlocked.get()
    val blockedAds: Int get() = _blockedAds.get()
    val blockedTrackers: Int get() = _blockedTrackers.get()
    val blockedAnalytics: Int get() = _blockedAnalytics.get()
    val blockedSocial: Int get() = _blockedSocial.get()
    val blockedSuspicious: Int get() = _blockedSuspicious.get()
    val blockedUnknown: Int get() = _blockedUnknown.get()

    fun recordChecked() {
        _totalRequestsChecked.incrementAndGet()
    }

    fun record(decision: BlockDecision) {
        if (decision == BlockDecision.ALLOW) return
        _totalBlocked.incrementAndGet()
        when (decision) {
            BlockDecision.BLOCK_AD -> _blockedAds.incrementAndGet()
            BlockDecision.BLOCK_TRACKER -> _blockedTrackers.incrementAndGet()
            BlockDecision.BLOCK_ANALYTICS -> _blockedAnalytics.incrementAndGet()
            BlockDecision.BLOCK_SOCIAL_TRACKER -> _blockedSocial.incrementAndGet()
            BlockDecision.BLOCK_MALWARE_OR_SUSPICIOUS -> _blockedSuspicious.incrementAndGet()
            BlockDecision.BLOCK_UNKNOWN -> _blockedUnknown.incrementAndGet()
            else -> {}
        }
    }

    fun reset() {
        _totalRequestsChecked.set(0)
        _totalBlocked.set(0)
        _blockedAds.set(0)
        _blockedTrackers.set(0)
        _blockedAnalytics.set(0)
        _blockedSocial.set(0)
        _blockedSuspicious.set(0)
        _blockedUnknown.set(0)
    }
}
