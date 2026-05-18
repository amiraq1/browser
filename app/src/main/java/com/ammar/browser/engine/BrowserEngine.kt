package com.ammar.browser.engine

import android.content.Context
import android.view.View

/**
 * Abstraction layer for the browser engine.
 * Currently backed by WebView; designed to be swapped with
 * Chromium custom or GeckoView in the future.
 */
interface BrowserEngine {
    fun initialize(context: Context)
    fun createEngineView(context: Context): View
    fun getView(): View?
    fun loadUrl(url: String)
    fun loadHtml(html: String, baseUrl: String? = null)
    fun goBack(): Boolean
    fun goForward(): Boolean
    fun reload()
    fun stopLoading()
    fun canGoBack(): Boolean
    fun canGoForward(): Boolean
    fun getCurrentUrl(): String?
    fun getTitle(): String?
    fun destroy()

    fun setCallback(callback: EngineCallback)
}

/**
 * Callback interface for engine events.
 * Each method receives the tabId so the consumer can decide
 * whether to update UI (active tab) or just data (background tab).
 */
interface EngineCallback {
    fun onPageStarted(tabId: String, url: String)
    fun onPageFinished(tabId: String, url: String)
    fun onProgressChanged(tabId: String, progress: Int)
    fun onTitleChanged(tabId: String, title: String?)
}
