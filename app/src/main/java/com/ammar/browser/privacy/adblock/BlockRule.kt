package com.ammar.browser.privacy.adblock

data class BlockRule(
    val domain: String,
    val decision: BlockDecision
)
