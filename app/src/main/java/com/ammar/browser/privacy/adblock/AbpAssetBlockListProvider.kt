package com.ammar.browser.privacy.adblock

import android.content.Context
import android.util.Log

/**
 * Loads and parses ABP-format filter list from assets.
 */
class AbpAssetBlockListProvider(
    private val context: Context,
    private val fileName: String = "blocklists/easylist_subset.txt"
) : BlockListProvider {

    var exceptions: Set<String> = emptySet()
        private set

    override fun getRules(): List<BlockRule> {
        return try {
            val lines = context.assets.open(fileName).bufferedReader().use { it.readLines() }
            val result = AbpFilterParser().parse(lines.asSequence())
            exceptions = result.exceptions
            Log.d("AbpAssetProvider", "Loaded ${result.blockRules.size} rules, ${result.exceptions.size} exceptions from $fileName")
            result.blockRules
        } catch (e: Exception) {
            Log.w("AbpAssetProvider", "Failed to load $fileName", e)
            emptyList()
        }
    }
}
