package com.ammar.browser.privacy

/**
 * Classifies blocked tracker hosts into known companies.
 * Session-only, no persistence.
 */
object TrackerCompanyClassifier {

    private val companyDomains = mapOf(
        "Google" to listOf(
            "doubleclick.net", "googlesyndication.com", "googletagmanager.com",
            "google-analytics.com", "googleadservices.com", "googletagservices.com",
            "adservice.google.com", "2mdn.net"
        ),
        "Meta" to listOf(
            "facebook.net", "connect.facebook.net", "fbcdn.net", "instagram.com"
        ),
        "Amazon" to listOf(
            "amazon-adsystem.com", "advertising.amazon.com"
        ),
        "Microsoft" to listOf(
            "clarity.ms", "bat.bing.com", "telemetry.microsoft.com",
            "vortex.data.microsoft.com", "dc.services.visualstudio.com"
        ),
        "TikTok" to listOf(
            "tiktok.com", "analytics.tiktok.com", "tiktokcdn.com"
        )
    )

    fun classify(host: String?): String {
        if (host.isNullOrBlank()) return "Other"
        val h = host.lowercase()
        for ((company, domains) in companyDomains) {
            for (d in domains) {
                if (h == d || h.endsWith(".$d")) return company
            }
        }
        return "Other"
    }

    fun summarize(hosts: List<String>): Map<String, Int> {
        val counts = mutableMapOf<String, Int>()
        hosts.forEach { counts.merge(classify(it), 1) { a, b -> a + b } }
        return counts
    }

    fun topCompany(hosts: List<String>): String {
        if (hosts.isEmpty()) return "None"
        val counts = summarize(hosts)
        return counts.maxByOrNull { it.value }?.key ?: "None"
    }
}
