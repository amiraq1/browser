package com.ammar.browser.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationHelperTest {

    @Test
    fun upgradeToHttps_upgradesCleartextHttp() {
        assertEquals(
            "https://example.com/path?q=1",
            NavigationHelper.upgradeToHttps("http://example.com/path?q=1")
        )
    }

    @Test
    fun upgradeToHttps_preservesSpecialSchemes() {
        assertEquals("ammar://newtab", NavigationHelper.upgradeToHttps("ammar://newtab"))
        assertEquals("about:blank", NavigationHelper.upgradeToHttps("about:blank"))
        assertEquals("data:text/plain,ok", NavigationHelper.upgradeToHttps("data:text/plain,ok"))
    }

    @Test
    fun resolveInput_prefersHttpsForBareDomains() {
        assertEquals("https://example.com", NavigationHelper.resolveInput("example.com"))
    }
}
