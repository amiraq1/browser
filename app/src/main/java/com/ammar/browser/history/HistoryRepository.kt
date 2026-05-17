package com.ammar.browser.history

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistoryRepository(context: Context) {

    private val dao = BrowserDatabase.getInstance(context).historyDao()

    suspend fun recordVisit(url: String, title: String) {
        if (!shouldSave(url)) return
        withContext(Dispatchers.IO) {
            val existing = dao.findByUrl(url)
            if (existing != null) {
                dao.upsert(existing.copy(
                    title = title.ifEmpty { existing.title },
                    visitCount = existing.visitCount + 1,
                    lastVisited = System.currentTimeMillis()
                ))
            } else {
                dao.upsert(HistoryEntity(url = url, title = title))
            }
        }
    }

    suspend fun getAll(): List<HistoryEntity> = withContext(Dispatchers.IO) {
        dao.getAll()
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        dao.clearAll()
    }

    private fun shouldSave(url: String): Boolean {
        if (url.isBlank()) return false
        if (url == "about:blank") return false
        if (!url.startsWith("http://") && !url.startsWith("https://")) return false
        return true
    }
}
