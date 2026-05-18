package com.ammar.browser.privacy

/**
 * Hides common cookie consent banners via CSS injection.
 * Does NOT click accept/reject. Visual hiding only.
 * TODO: Future: implement safe reject-all for known CMPs.
 */
object CookieBannerHider {

    private val HIDE_CSS = """
        .cookie-banner,.cookie-consent,.cookie-notice,.cookie-popup,
        #cookie-banner,#cookie-consent,#onetrust-banner-sdk,#onetrust-consent-sdk,
        .didomi-popup-container,.qc-cmp2-container,#qc-cmp2-ui,
        .truste_box_overlay,.truste_popframe,.cc-window,.osano-cm-window,
        .cmp-container,.consent-banner,.gdpr-banner,.privacy-banner,
        [class*="cookie-banner"],[class*="cookie-consent"],[id*="cookie-banner"],
        [id*="cookie-consent"],[class*="CookieBanner"],[class*="CookieConsent"]
        { display: none !important; visibility: hidden !important; }
        body { overflow: auto !important; }
    """.trimIndent().replace("\n", " ")

    val JS_INJECT = """
        (function(){
            var s=document.createElement('style');
            s.textContent='$HIDE_CSS';
            document.head.appendChild(s);
        })();
    """.trimIndent()
}
