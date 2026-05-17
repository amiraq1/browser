package com.ammar.browser.privacy.adblock

data class BlockedRequest(
    val url: String,
    val host: String,
    val pageUrl: String?,
    val decision: BlockDecision,
    val tabId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
