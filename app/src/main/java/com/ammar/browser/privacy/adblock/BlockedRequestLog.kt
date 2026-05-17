package com.ammar.browser.privacy.adblock

import java.util.LinkedList

/**
 * Thread-safe in-memory log of last N blocked requests.
 */
class BlockedRequestLog(private val maxSize: Int = 50) {

    private val log = LinkedList<BlockedRequest>()

    @Synchronized
    fun add(request: BlockedRequest) {
        log.addFirst(request)
        if (log.size > maxSize) log.removeLast()
    }

    @Synchronized
    fun getRecent(): List<BlockedRequest> = log.toList()

    @Synchronized
    fun clear() = log.clear()
}
