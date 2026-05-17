package com.ammar.browser.tabs

import java.util.UUID

data class Tab(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "",
    val title: String = "New Tab",
    val isLoading: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val isPrivate: Boolean = false
)
