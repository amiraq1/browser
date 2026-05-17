package com.ammar.browser.privacy.adblock

import android.content.Context
import android.util.Log

/**
 * Loads block rules from a text file in assets/blocklists/.
 * Format per line: domain,TYPE (comments start with #, blank lines ignored).
 */
class AssetBlockListProvider(
    private val context: Context,
    private val fileName: String = "blocklists/default_domains.txt"
) : BlockListProvider {

    override fun getRules(): List<BlockRule> {
        return try {
            val rules = mutableListOf<BlockRule>()
            context.assets.open(fileName).bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEach
                    val parts = trimmed.split(",", limit = 2)
                    if (parts.size == 2) {
                        val domain = parts[0].trim().lowercase()
                        val decision = parseType(parts[1].trim())
                        if (domain.isNotEmpty() && decision != null) {
                            rules.add(BlockRule(domain, decision))
                        }
                    }
                }
            }
            Log.d("AssetBlockList", "Loaded ${rules.size} block rules from assets")
            rules
        } catch (e: Exception) {
            Log.w("AssetBlockList", "Failed to load asset blocklist, using fallback", e)
            emptyList()
        }
    }

    private fun parseType(type: String): BlockDecision? = when (type.uppercase()) {
        "AD" -> BlockDecision.BLOCK_AD
        "TRACKER" -> BlockDecision.BLOCK_TRACKER
        "ANALYTICS" -> BlockDecision.BLOCK_ANALYTICS
        "SOCIAL" -> BlockDecision.BLOCK_SOCIAL_TRACKER
        "SUSPICIOUS" -> BlockDecision.BLOCK_MALWARE_OR_SUSPICIOUS
        "UNKNOWN" -> BlockDecision.BLOCK_UNKNOWN
        else -> null
    }
}
