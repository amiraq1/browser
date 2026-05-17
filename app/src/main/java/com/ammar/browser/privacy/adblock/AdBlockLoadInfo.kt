package com.ammar.browser.privacy.adblock

data class AdBlockLoadInfo(
    val defaultDomainRulesCount: Int,
    val abpRulesCount: Int,
    val abpExceptionsCount: Int,
    val totalRulesCount: Int
)
