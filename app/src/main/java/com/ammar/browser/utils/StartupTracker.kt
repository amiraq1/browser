package com.ammar.browser.utils

object StartupTracker {
    var lastStep: String = "not started"
        private set

    fun mark(step: String) {
        lastStep = step
    }
}
