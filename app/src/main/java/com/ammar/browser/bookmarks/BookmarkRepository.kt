package com.ammar.browser.bookmarks

import android.content.Context
import com.ammar.browser.history.BrowserDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository wrapping [BookmarkDao].
 *
 * Filters out non-bookmarkable URLs (new tab page, blank pages, data URIs,
 * non-http(s) schemes) at the boundary so callers don't have to repeat the
 * checks. URLs are deduplicated by their unique index on `url` — calling
 * [addOrUpdate] twice with the same URL updates the title and timestamp
 * instead of creating a second row.
 */
class BookmarkRepository(context: Context) {

    private val dao = BrowserDatabase.getInstance(context).bookmarkDao()

    /**
     * Result of an [addOrUpdate] call so the UI can show the right toast.
     */
    enum class AddResult { ADDED, UPDATED, SKIPPED }

    suspend fun addOrUpdate(url: String, title: String): AddResult {
        if (!shouldSave(url)) return AddResult.SKIPPED
        return withContext(Dispatchers.IO) {
            val existing = dao.findByUrl(url)
            if (existing != null) {
                dao.upsert(
                    existing.copy(
                        title = title.ifEmpty { existing.title },
                        lastUpdated = System.currentTimeMillis()
                    )
                )
                AddResult.UPDATED
            } else {
                dao.upsert(
                    BookmarkEntity(
                        url = url,
                        title = title.ifEmpty { url }
                    )
                )
                AddResult.ADDED
            }
        }
    }

    suspend fun getAll(): List<BookmarkEntity> = withContext(Dispatchers.IO) {
        dao.getAll()
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    suspend fun deleteByUrl(url: String) = withContext(Dispatchers.IO) {
        dao.deleteByUrl(url)
    }

    suspend fun isBookmarked(url: String): Boolean = withContext(Dispatchers.IO) {
        dao.findByUrl(url) != null
    }

    /**
     * Mirrors [com.ammar.browser.history.HistoryRepository.shouldSave] but is
     * a touch stricter: also rejects the new-tab page URL and `data:` URIs.
     */
    private fun shouldSave(url: String): Boolean {
        if (url.isBlank()) return false
        if (url == "about:blank") return false
        if (url == "ammar://newtab") return false
        if (url.startsWith("data:")) return false
        if (!url.startsWith("http://") && !url.startsWith("https://")) return false
        return true
    }
}
