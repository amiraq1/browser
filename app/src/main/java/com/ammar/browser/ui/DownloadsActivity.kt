package com.ammar.browser.ui

import android.app.DownloadManager
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ammar.browser.R
import com.ammar.browser.utils.applySystemBarPaddingToContent

/**
 * Downloads screen.
 *
 * Shows the most recent downloads (queried from Android DownloadManager)
 * and offers a button to open the system Downloads folder.
 *
 * Privacy: no file reads beyond DownloadManager metadata, no listFiles,
 * no path emission, no telemetry.
 */
class DownloadsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloads)
        applySystemBarPaddingToContent()
        supportActionBar?.apply {
            title = getString(R.string.downloads_title)
            setDisplayHomeAsUpEnabled(true)
        }

        findViewById<Button>(R.id.btn_open_downloads_folder).setOnClickListener {
            openDownloadsFolder()
        }

        loadRecentDownloads()
    }

    override fun onResume() {
        super.onResume()
        loadRecentDownloads()
    }

    /**
     * Queries Android DownloadManager for the most recent downloads
     * and displays them in the recent_downloads_container.
     */
    private fun loadRecentDownloads() {
        val container = findViewById<LinearLayout>(R.id.recent_downloads_container) ?: return
        container.removeAllViews()

        val dm = getSystemService(DOWNLOAD_SERVICE) as? DownloadManager
        if (dm == null) {
            addEmptyMessage(container)
            return
        }

        val query = DownloadManager.Query()
            .setFilterByStatus(
                DownloadManager.STATUS_SUCCESSFUL or
                DownloadManager.STATUS_FAILED or
                DownloadManager.STATUS_RUNNING or
                DownloadManager.STATUS_PAUSED or
                DownloadManager.STATUS_PENDING
            )

        val cursor: Cursor? = try {
            dm.query(query)
        } catch (_: Exception) {
            null
        }

        if (cursor == null || cursor.count == 0) {
            cursor?.close()
            addEmptyMessage(container)
            return
        }

        val titleIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
        val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        val bytesIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

        var count = 0
        val maxItems = 15

        while (cursor.moveToNext() && count < maxItems) {
            val title = if (titleIdx >= 0) cursor.getString(titleIdx) else null
            val status = if (statusIdx >= 0) cursor.getInt(statusIdx) else -1
            val bytes = if (bytesIdx >= 0) cursor.getLong(bytesIdx) else -1L

            if (title.isNullOrBlank()) continue

            val statusText = when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> "✓ Complete"
                DownloadManager.STATUS_RUNNING -> "⬇ Downloading"
                DownloadManager.STATUS_PAUSED -> "⏸ Paused"
                DownloadManager.STATUS_PENDING -> "⏳ Pending"
                DownloadManager.STATUS_FAILED -> "✗ Failed"
                else -> "Unknown"
            }

            val sizeText = if (bytes > 0) formatFileSize(bytes) else ""

            val itemView = TextView(this).apply {
                text = "$title\n$statusText${if (sizeText.isNotEmpty()) " • $sizeText" else ""}"
                textSize = 13f
                setTextColor(this@DownloadsActivity.getColor(R.color.nabd_text_primary))
                setPadding(0, dp(8), 0, dp(8))
                setLineSpacing(4f, 1f)
            }
            container.addView(itemView)

            // Divider
            if (count < maxItems - 1) {
                val divider = android.view.View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(1)
                    )
                    setBackgroundColor(0x33FFFFFF)
                }
                container.addView(divider)
            }

            count++
        }

        cursor.close()

        if (count == 0) {
            addEmptyMessage(container)
        }
    }

    private fun addEmptyMessage(container: LinearLayout) {
        val tv = TextView(this).apply {
            text = getString(R.string.downloads_empty)
            textSize = 14f
            setTextColor(this@DownloadsActivity.getColor(R.color.nabd_text_muted))
            gravity = Gravity.CENTER
            setPadding(0, dp(16), 0, dp(16))
        }
        container.addView(tv)
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1_048_576 -> String.format("%.1f MB", bytes / 1_048_576.0)
            bytes >= 1024 -> String.format("%.0f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    /**
     * Tries a small ladder of safe, read-only Intents to surface the
     * system Downloads view (or the Downloads folder in a file picker).
     */
    private fun openDownloadsFolder() {
        if (tryStartActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))) return

        val downloadsUri = Uri.fromFile(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        )
        val viewFolder = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(downloadsUri, "resource/folder")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (tryStartActivity(viewFolder)) return

        val openDoc = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        if (tryStartActivity(openDoc)) return

        Toast.makeText(this, R.string.downloads_open_failed, Toast.LENGTH_SHORT).show()
    }

    private fun tryStartActivity(intent: Intent): Boolean {
        return try {
            startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
