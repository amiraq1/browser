package com.example.browser

import android.net.Uri
import android.util.Log
import java.util.HashSet

class AdBlocker {
    private val adDomains: HashSet<String> = HashSet()
    private val blockedPatterns: List<String> = listOf(
        "/ads/",
        "/ad/",
        "/banner/",
        "/banners/",
        "/popup/",
        "/popunder/",
        "/prebid",
        "/tracking/",
        "/track/",
        "/analytics/",
        "ad_type=",
        "ad_client=",
        "ad_unit=",
        "clickid=",
        "campaign=",
        "utm_source=",
        "google_ads",
        "doubleclick",
        "pagead"
    )

    init {
        // Load common ad domains
        adDomains.add("doubleclick.net")
        adDomains.add("googlesyndication.com")
        adDomains.add("googleadservices.com")
        adDomains.add("adservice.google.com")
        adDomains.add("pagead2.googlesyndication.com")
        adDomains.add("adsystem.com")
        adDomains.add("adsterra.com")
        adDomains.add("popads.net")
        adDomains.add("propellerads.com")
        adDomains.add("onclickads.net")
        adDomains.add("taboola.com")
        adDomains.add("outbrain.com")
        adDomains.add("mgid.com")
        adDomains.add("exoclick.com")
        adDomains.add("admaven.com")
        adDomains.add("push-ad.com")
        adDomains.add("revcontent.com")
        adDomains.add("smartadserver.com")
        adDomains.add("scorecardresearch.com")
        adDomains.add("adsafeprotected.com")
        adDomains.add("serving-sys.com")
        adDomains.add("criteo.com")
        adDomains.add("criteo.net")
        adDomains.add("yieldmo.com")
        adDomains.add("media.net")
        adDomains.add("inmobi.com")
        adDomains.add("unityads.unity3d.com")
        adDomains.add("applovin.com")
        adDomains.add("ironsrc.com")
        adDomains.add("vungle.com")
        adDomains.add("chartboost.com")
        adDomains.add("startappservice.com")
        adDomains.add("adnxs.com")
        adDomains.add("carbonads.net")
        adDomains.add("pubmatic.com")
        adDomains.add("rubiconproject.com")
        adDomains.add("openx.net")
    }

    fun isAd(url: String?): Boolean {
        if (url == null) return false
        val uri = Uri.parse(url)
        val host = uri.host?.lowercase() ?: ""
        
        // 1. Check Domain (Host)
        var currentHost = host
        while (currentHost.contains(".")) {
            if (adDomains.contains(currentHost)) {
                Log.d("AdBlocker", "Blocked domain: $url")
                return true
            }
            currentHost = currentHost.substring(currentHost.indexOf(".") + 1)
        }
        
        // 2. Check Patterns in Path and Query
        val fullPath = uri.path ?: ""
        val fullQuery = uri.query ?: ""
        
        for (pattern in blockedPatterns) {
            if (fullPath.contains(pattern, ignoreCase = true) || 
                fullQuery.contains(pattern, ignoreCase = true)) {
                Log.d("AdBlocker", "Blocked pattern ($pattern): $url")
                return true
            }
        }

        return false
    }
}
