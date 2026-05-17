package com.ammar.browser.privacy.adblock

object DefaultBlockLists {

    val ads = listOf(
        "doubleclick.net",
        "googlesyndication.com",
        "adservice.google.com",
        "googletagservices.com",
        "adform.net",
        "adsystem.com",
        "amazon-adsystem.com",
        "rubiconproject.com",
        "pubmatic.com",
        "openx.net",
        "taboola.com",
        "outbrain.com",
        "revcontent.com"
    )

    val analytics = listOf(
        "google-analytics.com",
        "googletagmanager.com",
        "scorecardresearch.com",
        "hotjar.com",
        "mixpanel.com",
        "segment.io",
        "amplitude.com"
    )

    val socialTrackers = listOf(
        "facebook.net",
        "connect.facebook.net",
        "twitter.com/i/adsct",
        "tiktok.com/i18n/pixel",
        "snapchat.com/tr",
        "linkedin.com/px"
    )

    fun buildRules(): List<BlockRule> {
        val rules = mutableListOf<BlockRule>()
        ads.forEach { rules.add(BlockRule(it, BlockDecision.BLOCK_AD)) }
        analytics.forEach { rules.add(BlockRule(it, BlockDecision.BLOCK_ANALYTICS)) }
        socialTrackers.forEach { rules.add(BlockRule(it, BlockDecision.BLOCK_SOCIAL_TRACKER)) }
        return rules
    }
}
