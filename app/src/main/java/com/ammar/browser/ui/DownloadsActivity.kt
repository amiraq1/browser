package com.ammar.browser.ui

import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ammar.browser.R

/**
 * Lightweight, informational Downloads screen.
 *
 * The screen does NOT enumerate the user's files, request storage
 * permissions, or maintain any download queue. It only:
 *
 *   1. Tells the user where Nabd downloads are saved.
 *   2. Offers a single button that tries to open the system Downloads
 *      view via well-known Intents and falls back to a Toast on failure.
 *
 * Privacy: no file reads, no listFiles, no path emission, no telemetry.
 */
class DownloadsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloads)
        supportActionBar?.apply {
            title = getString(R.string.downloads_title)
            setDisplayHomeAsUpEnabled(true)
        }

        findViewById<Button>(R.id.btn_open_downloads_folder).setOnClickListener {
            openDownloadsFolder()
        }
    }

    /**
     * Tries a small ladder of safe, read-only Intents to surface the
     * system Downloads view (or the Downloads folder in a file picker).
     * Stops at the first one that resolves on the device. Each Intent
     * is guarded with a try/catch so a missing handler never crashes.
     */
    private fun openDownloadsFolder() {
        // 1. Stock Android Downloads UI (handled by DocumentsUI / Files).
        if (tryStartActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))) return

        // 2. ACTION_VIEW on the public Downloads directory URI.
        val downloadsUri = Uri.fromFile(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        )
        val viewFolder = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(downloadsUri, "resource/folder")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (tryStartActivity(viewFolder)) return

        // 3. Generic file-picker style fallback (read-only, system handles it).
        val openDoc = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        if (tryStartActivity(openDoc)) return

        // All Intents failed — surface a friendly Toast, never a crash.
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
