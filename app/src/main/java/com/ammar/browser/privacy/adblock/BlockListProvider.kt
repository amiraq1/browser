package com.ammar.browser.privacy.adblock

/**
 * Provides block rules. Can be extended to load EasyList, custom lists, etc.
 */
interface BlockListProvider {
    fun getRules(): List<BlockRule>
}
