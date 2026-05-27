package com.ammar.browser.search

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchEngineTest {

    @Test
    fun fromId_fallsBackToDuckDuckGo() {
        assertEquals(SearchEngine.DUCKDUCKGO, SearchEngine.fromId(null))
        assertEquals(SearchEngine.DUCKDUCKGO, SearchEngine.fromId("missing"))
    }

    @Test
    fun searchUrl_encodesQuery() {
        assertEquals(
            "https://duckduckgo.com/?q=privacy+browser",
            SearchEngine.DUCKDUCKGO.searchUrl("privacy browser")
        )
    }
}
