package com.ammar.browser.privacy

/**
 * Privacy configuration for the browser engine.
 * Will be expanded with: tracker blocking, cookie policy, fingerprint protection.
 */
data class PrivacyConfig(
    val blockThirdPartyCookies: Boolean = true,
    val doNotTrack: Boolean = true,
    val clearDataOnExit: Boolean = false
)
