package com.example.browser

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayInputStream

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var urlEditText: EditText
    private lateinit var goButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var adBlockStatus: TextView
    private lateinit var backButton: ImageButton
    private lateinit var forwardButton: ImageButton
    private lateinit var refreshButton: ImageButton
    private lateinit var homeButton: ImageButton

    private val adBlocker = AdBlocker()
    private var blockedCount = 0

    private val homeUrl = "file:///android_asset/home.html"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupWebView()
        setupListeners()

        webView.loadUrl(homeUrl)
    }

    private fun initViews() {
        webView = findViewById(R.id.webView)
        urlEditText = findViewById(R.id.urlEditText)
        goButton = findViewById(R.id.goButton)
        progressBar = findViewById(R.id.progressBar)
        adBlockStatus = findViewById(R.id.adBlockStatus)
        backButton = findViewById(R.id.backButton)
        forwardButton = findViewById(R.id.forwardButton)
        refreshButton = findViewById(R.id.refreshButton)
        homeButton = findViewById(R.id.homeButton)
    }

    private fun setupWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.setSupportMultipleWindows(false)
        settings.javaScriptCanOpenWindowsAutomatically = false
        
        // Privacy settings
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false)
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                
                if (adBlocker.isAd(url)) {
                    Log.d("AdBlocker", "Blocked redirect: $url")
                    runOnUiThread {
                        blockedCount++
                        updateAdBlockCounter()
                    }
                    return true // Block the navigation
                }
                
                // Allow normal navigation within the WebView
                return false 
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val url = request?.url?.toString()
                if (adBlocker.isAd(url)) {
                    runOnUiThread {
                        blockedCount++
                        updateAdBlockCounter()
                    }
                    // Return an empty response to block the request
                    return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray()))
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
                urlEditText.setText(url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                hideAdElements()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
            }

            override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: android.os.Message?): Boolean {
                // Prevent creating new windows (popups)
                return false
            }
        }
    }

    private fun hideAdElements() {
        Log.d("AdBlocker", "Injected visual ad blocker")
        val js = """
            (function() {
                const adKeywords = [
                    "Your Prize is READY", "Congratulations", "Claim your reward",
                    "Tap to see more", "reward", "Prize", "Ad"
                ];
                
                const adSelectors = [
                    '[id*="ad"]', '[class*="ad"]', '[id*="reward"]', '[class*="reward"]',
                    '[id*="prize"]', '[class*="prize"]', '[class*="modal"]', '[class*="overlay"]',
                    '[class*="interstitial"]', '[class*="getbutton"]', '[id*="getbutton"]',
                    'iframe', 'ins.adsbygoogle', '.ad', '.ads', '.adsbygoogle', '.advertisement', 
                    '.banner', '.popup', '.pop', '.sponsor', '.sponsored', '.native-ad',
                    '.ad-container', '.ad-wrapper'
                ];

                const cleanPage = () => {
                    // 1. Hide by specific ad selectors
                    adSelectors.forEach(selector => {
                        try {
                            document.querySelectorAll(selector).forEach(el => {
                                if (el.tagName !== 'BODY' && el.tagName !== 'HTML') {
                                    el.style.setProperty('display', 'none', 'important');
                                    el.style.setProperty('visibility', 'hidden', 'important');
                                    el.style.setProperty('height', '0', 'important');
                                }
                            });
                        } catch (e) {}
                    });

                    // 2. Scan for ad keywords and fixed/sticky overlays
                    document.querySelectorAll('div, section, aside, span, a, button, ins').forEach(el => {
                        try {
                            // Check text content (case insensitive check for short words)
                            const text = el.innerText || "";
                            const hasKeyword = adKeywords.some(kw => {
                                if (kw.length <= 3) return text === kw || text.trim() === kw;
                                return text.includes(kw);
                            });

                            if (hasKeyword && el.children.length < 10) {
                                el.style.setProperty('display', 'none', 'important');
                            }

                            // Detect fixed/sticky overlays (common for popups/interstitials)
                            const style = window.getComputedStyle(el);
                            const position = style.position;
                            const zIndex = parseInt(style.zIndex) || 0;
                            
                            if ((position === 'fixed' || position === 'sticky') && zIndex > 10) {
                                const rect = el.getBoundingClientRect();
                                // Check if it covers a large part of the screen or looks like a banner
                                if (rect.height < 150 || (rect.width > window.innerWidth * 0.8 && rect.height > window.innerHeight * 0.5)) {
                                    el.style.setProperty('display', 'none', 'important');
                                }
                            }
                        } catch (e) {}
                    });
                };

                // Inject CSS for immediate performance
                let styleTag = document.getElementById('browser-adblock-style');
                if (!styleTag) {
                    styleTag = document.createElement('style');
                    styleTag.id = 'browser-adblock-style';
                    styleTag.innerHTML = adSelectors.join(', ') + " { display: none !important; visibility: hidden !important; height: 0 !important; }";
                    document.head.appendChild(styleTag);
                }

                // Initial cleanup
                cleanPage();

                // Monitor for dynamic changes
                const observer = new MutationObserver(cleanPage);
                observer.observe(document.body, { childList: true, subtree: true });

                // Periodic cleanup for 15 seconds after load
                let attempts = 0;
                const interval = setInterval(() => {
                    cleanPage();
                    if (++attempts > 15) clearInterval(interval);
                }, 1000);
            })();
        """.trimIndent()

        webView.evaluateJavascript(js, null)
    }

    private fun setupListeners() {
        goButton.setOnClickListener {
            loadUrlFromInput()
        }

        urlEditText.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                loadUrlFromInput()
                true
            } else {
                false
            }
        }

        backButton.setOnClickListener {
            if (webView.canGoBack()) webView.goBack()
        }

        forwardButton.setOnClickListener {
            if (webView.canGoForward()) webView.goForward()
        }

        refreshButton.setOnClickListener {
            webView.reload()
        }

        homeButton.setOnClickListener {
            webView.loadUrl(homeUrl)
            blockedCount = 0
            updateAdBlockCounter()
        }
    }

    private fun loadUrlFromInput() {
        var url = urlEditText.text.toString().trim()
        if (url.isNotEmpty()) {
            if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("file://")) {
                url = if (url.contains(".") && !url.contains(" ")) {
                    "https://$url"
                } else {
                    "https://www.google.com/search?q=$url"
                }
            }
            webView.loadUrl(url)
        }
    }

    private fun updateAdBlockCounter() {
        adBlockStatus.text = getString(R.string.blocked_status, blockedCount)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
