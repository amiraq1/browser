package com.ammar.browser.privacy.adblock

import android.util.Log

/**
 * Parses a subset of ABP/EasyList filter syntax.
 *
 * Supported:
 *   ||domain.com^        → block domain
 *   @@||domain.com^      → exception (allow) domain
 *   ! comment            → ignored
 *   [Adblock Plus ...]   → ignored header
 *   blank lines          → ignored
 *
 * Unsupported (silently ignored):
 *   ##.class             → cosmetic filters
 *   #@#                  → cosmetic exceptions
 *   $options             → advanced options
 *   /regex/              → regex rules
 *   wildcard patterns    → complex wildcards
 */
class AbpFilterParser {

    data class ParseResult(
        val blockRules: List<BlockRule>,
        val exceptions: Set<String>,
        val parsedCount: Int,
        val ignoredCount: Int
    )

    fun parse(lines: Sequence<String>): ParseResult {
        val blockRules = mutableListOf<BlockRule>()
        val exceptions = mutableSetOf<String>()
        var parsed = 0
        var ignored = 0

        lines.forEach { raw ->
            val line = raw.trim()
            when {
                line.isEmpty() -> {}
                line.startsWith("!") -> {}
                line.startsWith("[") -> {}
                line.startsWith("@@||") -> {
                    extractDomain(line.removePrefix("@@||"))?.let {
                        exceptions.add(it)
                        parsed++
                    } ?: ignored++
                }
                line.startsWith("||") -> {
                    extractDomain(line.removePrefix("||"))?.let {
                        blockRules.add(BlockRule(it, BlockDecision.BLOCK_AD))
                        parsed++
                    } ?: ignored++
                }
                else -> ignored++
            }
        }

        Log.d("AbpFilterParser", "Parsed: $parsed rules, ${exceptions.size} exceptions, $ignored ignored")
        return ParseResult(blockRules, exceptions, parsed, ignored)
    }

    /**
     * Extracts domain from "domain.com^" or "domain.com^$options".
     * Returns null if format is unsupported.
     */
    private fun extractDomain(raw: String): String? {
        // Take everything before ^ or $ or end
        val domain = raw.substringBefore("^").substringBefore("$").trim().lowercase()
        if (domain.isEmpty()) return null
        // Skip if contains wildcards or path separators that indicate complex rules
        if (domain.contains("*") || domain.contains("/") || domain.contains("?")) return null
        return domain
    }
}
