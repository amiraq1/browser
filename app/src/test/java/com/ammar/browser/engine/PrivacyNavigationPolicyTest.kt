package com.ammar.browser.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyNavigationPolicyTest {

    @Test
    fun secureTopLevelUrl_upgradesHttpBeforeWebViewLoadsIt() {
        assertEquals(
            "https://insecure.example",
            PrivacyNavigationPolicy.secureTopLevelUrl("http://insecure.example")
        )
    }

    @Test
    fun shouldHandleAsTopLevelWebNavigation_onlyHandlesHttpAndHttps() {
        assertTrue(PrivacyNavigationPolicy.shouldHandleAsTopLevelWebNavigation("http://example.com"))
        assertTrue(PrivacyNavigationPolicy.shouldHandleAsTopLevelWebNavigation("https://example.com"))
        assertFalse(PrivacyNavigationPolicy.shouldHandleAsTopLevelWebNavigation("mailto:test@example.com"))
        assertFalse(PrivacyNavigationPolicy.shouldHandleAsTopLevelWebNavigation("ammar://newtab"))
    }

    @Test
    fun privacyHeaders_includeDntAndGpc() {
        assertEquals("1", PrivacyNavigationPolicy.PRIVACY_HEADERS["DNT"])
        assertEquals("1", PrivacyNavigationPolicy.PRIVACY_HEADERS["Sec-GPC"])
    }

    @Test
    fun localActionName_acceptsOnlyNonEmptyLocalActions() {
        assertEquals("settings", PrivacyNavigationPolicy.localActionName("ammar://action/settings"))
        assertEquals("settings", PrivacyNavigationPolicy.localActionName("ammar://action/settings/"))
        assertNull(PrivacyNavigationPolicy.localActionName("ammar://action/"))
        assertNull(PrivacyNavigationPolicy.localActionName("https://example.com/action/settings"))
    }
}
