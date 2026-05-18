package com.ammar.browser.search

import java.net.URLEncoder

/**
 * Supported search engines for the URL/search bar and the new tab page.
 *
 * [urlTemplate] is the prefix string the query is appended to (already
 * URL-encoded). It is intentionally `val` (not `private`) so the new tab
 * page can embed it directly into its inline JavaScript without needing
 * a JS bridge to the native side.
 */
enum class SearchEngine(
    val id: String,
    val displayName: String,
    val urlTemplate: String
) {
    DUCKDUCKGO("duckduckgo", "DuckDuckGo", "https://duckduckgo.com/?q="),
    BRAVE("brave", "Brave Search", "https://search.brave.com/search?q="),
    STARTPAGE("startpage", "Startpage", "https://www.startpage.com/sp/search?query="),
    GOOGLE("google", "Google", "https://www.google.com/search?q=");

    /** Builds a full search URL for the given query (URL-encodes the query). */
    fun searchUrl(query: String): String =
        urlTemplate + URLEncoder.encode(query, "UTF-8")

    companion object {
        /** Maps a stored id back to the enum, falling back to DuckDuckGo. */
        fun fromId(id: String?): SearchEngine =
            values().firstOrNull { it.id == id } ?: DUCKDUCKGO
    }
}
