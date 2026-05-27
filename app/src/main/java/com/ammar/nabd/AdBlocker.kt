package com.ammar.nabd

import android.net.Uri
import android.util.Log
import java.util.HashSet

class AdBlocker {
    private val adDomains: HashSet<String> = HashSet()
    private val blockedPatterns: MutableList<String> = mutableListOf()
    private val exceptionRules: MutableList<String> = mutableListOf()
    private val cosmeticRules: MutableList<String> = mutableListOf()
    
    var rulesCount = 0
        private set

    fun loadRules(content: String) {
        val lines = content.lines()
        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("!")) continue

            rulesCount++

            when {
                trimmedLine.startsWith("@@") -> {
                    // Exception rule
                    exceptionRules.add(trimmedLine.substring(2))
                }
                trimmedLine.startsWith("##") -> {
                    // Cosmetic rule
                    cosmeticRules.add(trimmedLine.substring(2))
                }
                trimmedLine.startsWith("||") -> {
                    // Domain-suffix rule
                    val domain = trimmedLine.substring(2).removeSuffix("^")
                    adDomains.add(domain)
                }
                else -> {
                    // Pattern rule
                    blockedPatterns.add(trimmedLine)
                }
            }
        }
        Log.d("AdBlocker", "Loaded $rulesCount rules from EasyList Lite")
    }

    fun isAd(url: String?): Boolean {
        if (url == null) return false
        
        // 1. Check Exception Rules (@@)
        for (rule in exceptionRules) {
            if (matchRule(url, rule)) {
                Log.d("AdBlocker", "Allowed by exception rule: $rule -> $url")
                return false
            }
        }

        val uri = Uri.parse(url)
        val host = uri.host?.lowercase() ?: ""
        
        // 2. Check Domain (Host) using ||domain^ style
        var currentHost = host
        while (currentHost.contains(".")) {
            if (adDomains.contains(currentHost)) {
                Log.d("AdBlocker", "Blocked by EasyList domain rule: $currentHost -> $url")
                return true
            }
            currentHost = currentHost.substring(currentHost.indexOf(".") + 1)
        }
        
        // 3. Check Patterns in Path and Query
        for (pattern in blockedPatterns) {
            if (matchRule(url, pattern)) {
                Log.d("AdBlocker", "Blocked by EasyList pattern rule: $pattern -> $url")
                return true
            }
        }

        return false
    }

    private fun matchRule(url: String, rule: String): Boolean {
        var cleanRule = rule
        
        // Handle basic wildcard matching
        if (cleanRule.startsWith("|") && !cleanRule.startsWith("||")) {
            return url.startsWith(cleanRule.substring(1))
        }
        
        val regex = cleanRule
            .replace(".", "\\.")
            .replace("*", ".*")
            .replace("?", "\\?")
            .replace("^", "($|[^a-zA-Z0-9_\\.%-])") // Simplified separator matching
        
        return try {
            url.contains(Regex(regex, RegexOption.IGNORE_CASE))
        } catch (e: Exception) {
            url.contains(cleanRule.replace("*", ""), ignoreCase = true)
        }
    }

    fun getCosmeticSelectors(): List<String> {
        return cosmeticRules
    }
}
