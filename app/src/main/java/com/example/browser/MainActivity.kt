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
        loadAdRules()
        setupWebView()
        setupListeners()

        webView.loadUrl(homeUrl)
    }

    private fun loadAdRules() {
        try {
            val content = assets.open("filters/easylist-lite.txt").bufferedReader().use { it.readText() }
            adBlocker.loadRules(content)
        } catch (e: Exception) {
            Log.e("AdBlocker", "Failed to load EasyList Lite", e)
        }
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
                injectAntiAdblockBypass()
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

    private fun injectAntiAdblockBypass() {
        Log.d("AdBlocker", "Injected anti-adblock bypass")
        val js = """
            (function() {
                const antiAdblockKeywords = [
                    "disable adblock", "adblock detected", "please disable adblock",
                    "turn off ad blocker", "ad blocker detected", "whitelist us",
                    "أوقف مانع الإعلانات", "يرجى تعطيل مانع الإعلانات", "تم اكتشاف مانع الإعلانات"
                ];

                const antiAdblockSelectors = [
                    '[class*="adblock"]', '[id*="adblock"]', '[class*="anti-adblock"]',
                    '[id*="anti-adblock"]', '[class*="ad-block"]', '[id*="ad-block"]',
                    '[class*="whitelist"]', '[id*="whitelist"]', '[class*="blocker"]', '[id*="blocker"]'
                ];

                const bypass = () => {
                    // 1. Remove by selectors
                    antiAdblockSelectors.forEach(selector => {
                        try {
                            document.querySelectorAll(selector).forEach(el => {
                                if (!['BODY', 'HTML', 'MAIN', 'ARTICLE'].includes(el.tagName)) {
                                    el.remove();
                                }
                            });
                        } catch (e) {}
                    });

                    // 2. Remove by keyword detection (with parent removal)
                    document.querySelectorAll('body *').forEach(el => {
                        try {
                            if (!el || !el.getBoundingClientRect) return;
                            const tag = el.tagName.toLowerCase();
                            if (['html', 'body', 'main', 'article'].includes(tag)) return;

                            const text = (el.innerText || el.textContent || "").toLowerCase();
                            const isMatch = antiAdblockKeywords.some(kw => text.includes(kw.toLowerCase()));
                            
                            if (isMatch) {
                                let target = el;
                                for (let i = 0; i < 3; i++) {
                                    if (target.parentElement && target.parentElement !== document.body) {
                                        target = target.parentElement;
                                    }
                                }
                                target.remove();
                            }
                        } catch (e) {}
                    });

                    // 3. Reset overflow if anti-adblock locked scrolling
                    if (document.body.style.overflow === 'hidden' || document.documentElement.style.overflow === 'hidden') {
                        document.body.style.setProperty('overflow', 'auto', 'important');
                        document.documentElement.style.setProperty('overflow', 'auto', 'important');
                    }
                };

                // Initial run
                bypass();

                // Monitor for dynamic anti-adblock popups
                const observer = new MutationObserver(bypass);
                observer.observe(document.body, { childList: true, subtree: true });

                // Polling for 20 seconds
                let attempts = 0;
                const interval = setInterval(() => {
                    bypass();
                    if (++attempts > 40) clearInterval(interval); // 500ms * 40 = 20s
                }, 500);
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    private fun hideAdElements() {
        Log.d("AdBlocker", "Injected visual ad blocker")
        val cosmeticSelectors = adBlocker.getCosmeticSelectors().joinToString("', '") { it }
        val js = """
            (function() {
                const badTexts = [
                    'Your Prize is READY', 'Congratulations', 'Claim your reward',
                    'Tap to see more', 'reward', 'Prize', 'Win ', 'Free '
                ];

                const adSelectors = ['$cosmeticSelectors'];

                const removeBadElements = () => {
                    document.querySelectorAll('body *').forEach(el => {
                        try {
                            const text = (el.innerText || '').trim();
                            const style = window.getComputedStyle(el);
                            const z = parseInt(style.zIndex || '0');
                            const rect = el.getBoundingClientRect();

                            // 1. Check for ad keywords (including Arabic)
                            const hasBadText = badTexts.some(t =>
                                text.toLowerCase().includes(t.toLowerCase())
                            );

                            const hasAdLabel =
                                text === 'Ad' || text === 'AD' || 
                                text.includes(' Ad ') || text.includes('إعلان') ||
                                (text.length < 10 && (text.includes('×') || text.includes('x')));

                            // 2. Detect floating/sticky ads (especially top banners)
                            const isFloatingAd =
                                (style.position === 'fixed' || style.position === 'sticky') &&
                                z > 50 &&
                                (
                                  (rect.top <= 10 && rect.height < 150) || // Top banner
                                  (rect.bottom >= window.innerHeight - 10 && rect.height < 150) || // Bottom banner
                                  (rect.width > window.innerWidth * 0.9 && z > 100) // Large overlay
                                );

                            // 3. Target Telegram/WhatsApp/GetButton widgets
                            const isSocialWidget = 
                                el.href?.includes('t.me/') || 
                                el.href?.includes('wa.me/') ||
                                el.id?.includes('getbutton') ||
                                el.className?.toString().includes('getbutton');

                            if (hasBadText || isFloatingAd || hasAdLabel || isSocialWidget) {
                                // Double check it's not the main content (rough heuristic)
                                if (el.tagName !== 'BODY' && el.tagName !== 'HTML' && el.children.length < 20) {
                                    el.remove();
                                }
                            }
                        } catch (e) {}
                    });
                    
                    // Specific CSS cleanup for instmod.com and similar sites
                    const selectors = [
                        '[id*="ad-"]', '[class*="ad-"]', '.adsbygoogle', 'iframe[src*="ads"]',
                        '.top-ad', '.header-ad', '.footer-ad', '#telegram-widget', '.getbutton-widget'
                    ];
                    selectors.forEach(sel => {
                        document.querySelectorAll(sel).forEach(el => el.remove());
                    });
                };

                // Initial run
                removeBadElements();

                // Monitor for dynamic changes
                const observer = new MutationObserver(removeBadElements);
                observer.observe(document.body, { childList: true, subtree: true });

                // Frequent cleanup for the first 15 seconds
                let attempts = 0;
                const interval = setInterval(() => {
                    removeBadElements();
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
            android.widget.Toast.makeText(this, "Rules loaded: ${adBlocker.rulesCount}", android.widget.Toast.LENGTH_SHORT).show()
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
