package com.ammar.browser.engine

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.ammar.browser.privacy.adblock.AdBlocker
import com.ammar.browser.privacy.adblock.BlockDecision
import java.io.ByteArrayInputStream

/**
 * WebView-based engine implementation.
 * Each instance is bound to a specific tab via [tabId].
 */
class WebViewEngine(
    private val tabId: String,
    private val adBlocker: AdBlocker? = null
) : BrowserEngine {

    private var webView: WebView? = null
    private var callback: EngineCallback? = null

    @Volatile
    private var currentPageUrl: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun initialize(context: Context) {}

    @SuppressLint("SetJavaScriptEnabled")
    override fun createEngineView(context: Context): View {
        webView?.let { return it }
        val wv = WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                loadWithOverviewMode = true
                useWideViewPort = true
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                setSupportMultipleWindows(false)
                // Privacy defaults
                allowFileAccess = false
                allowContentAccess = false
                @Suppress("DEPRECATION")
                allowFileAccessFromFileURLs = false
                @Suppress("DEPRECATION")
                allowUniversalAccessFromFileURLs = false
                setGeolocationEnabled(false)
            }
            // Block third-party cookies, allow first-party
            android.webkit.CookieManager.getInstance().setAcceptCookie(true)
            android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    currentPageUrl = url
                    url?.let { callback?.onPageStarted(tabId, it) }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    currentPageUrl = url
                    url?.let { callback?.onPageFinished(tabId, it) }
                    if (com.ammar.browser.privacy.CookieBannerSettings.enabled && url != null &&
                        !url.startsWith("about:") && !url.startsWith("ammar:") &&
                        !url.startsWith("data:") && !url.startsWith("file:")) {
                        view?.evaluateJavascript(com.ammar.browser.privacy.CookieBannerHider.JS_INJECT, null)
                    }
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val urlStr = request?.url?.toString() ?: return false
                    if (urlStr.startsWith("ammar://action/")) {
                        // Origin gate: only honor native actions when the
                        // current page is itself a local ammar:// page.
                        // This prevents arbitrary third-party sites from
                        // triggering native actions via redirects, iframes,
                        // or window.location.href.
                        val pageUrl = currentPageUrl
                        if (pageUrl != null && pageUrl.startsWith("ammar://")) {
                            val action = urlStr
                                .removePrefix("ammar://action/")
                                .trimEnd('/')
                            if (action.isNotEmpty()) {
                                callback?.onCustomAction(tabId, action)
                            }
                        }
                        // Always swallow so the WebView never tries to
                        // navigate to ammar://action/* (would error out).
                        return true
                    }
                    return false
                }

                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    val requestUrl = request?.url?.toString() ?: return null
                    val pageUrl = currentPageUrl
                    val decision = adBlocker?.shouldBlock(requestUrl, pageUrl, tabId)
                        ?: return null

                    if (decision != BlockDecision.ALLOW) {
                        return EMPTY_RESPONSE
                    }
                    return null
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    callback?.onProgressChanged(tabId, newProgress)
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    callback?.onTitleChanged(tabId, title)
                }
            }
        }
        webView = wv
        return wv
    }

    override fun getView(): View? = webView
    override fun loadUrl(url: String) {
        val upgraded = com.ammar.browser.navigation.NavigationHelper.upgradeToHttps(url)
        val headers = mapOf("DNT" to "1", "Sec-GPC" to "1")
        webView?.loadUrl(upgraded, headers)
    }
    override fun loadHtml(html: String, baseUrl: String?) {
        webView?.loadDataWithBaseURL(baseUrl, html, "text/html", "UTF-8", null)
    }

    override fun goBack(): Boolean {
        if (webView?.canGoBack() == true) { webView?.goBack(); return true }
        return false
    }

    override fun goForward(): Boolean {
        if (webView?.canGoForward() == true) { webView?.goForward(); return true }
        return false
    }

    override fun reload() { webView?.reload() }
    override fun stopLoading() { webView?.stopLoading() }
    override fun canGoBack(): Boolean = webView?.canGoBack() == true
    override fun canGoForward(): Boolean = webView?.canGoForward() == true
    override fun getCurrentUrl(): String? = webView?.url
    override fun getTitle(): String? = webView?.title

    override fun destroy() {
        webView?.destroy()
        webView = null
    }

    override fun setCallback(callback: EngineCallback) {
        this.callback = callback
    }

    companion object {
        private val EMPTY_RESPONSE = WebResourceResponse(
            "text/plain", "utf-8", ByteArrayInputStream(ByteArray(0))
        )
    }
}
