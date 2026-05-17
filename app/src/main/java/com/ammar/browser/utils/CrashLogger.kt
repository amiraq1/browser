package com.ammar.browser.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashLogger {

    private const val TAG = "AmmarCrash"
    private var appContext: Context? = null

    fun install(context: Context) {
        appContext = context.applicationContext
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val report = buildReport(thread, throwable)
                Log.e(TAG, report)
                writeReport(report)
            } catch (_: Exception) {
                // Never crash inside crash handler
            }
            previousHandler?.uncaughtException(thread, throwable)
                ?: android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    private fun buildReport(thread: Thread, throwable: Throwable): String {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))

        val ctx = appContext
        val versionName = try {
            ctx?.packageManager?.getPackageInfo(ctx.packageName, 0)?.versionName ?: "?"
        } catch (_: Exception) { "?" }

        val versionCode = try {
            ctx?.packageManager?.getPackageInfo(ctx.packageName, 0)?.let {
                if (Build.VERSION.SDK_INT >= 28) it.longVersionCode else @Suppress("DEPRECATION") it.versionCode.toLong()
            } ?: 0
        } catch (_: Exception) { 0 }

        return buildString {
            appendLine("=== Ammar Browser Crash Report ===")
            appendLine("Timestamp: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}")
            appendLine("Package: ${ctx?.packageName ?: "?"}")
            appendLine("Version: $versionName ($versionCode)")
            appendLine("Android SDK: ${Build.VERSION.SDK_INT}")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Thread: ${thread.name}")
            appendLine("Last startup step: ${StartupTracker.lastStep}")
            appendLine()
            appendLine("=== Exception ===")
            appendLine("${throwable.javaClass.name}: ${throwable.message}")
            appendLine()
            appendLine("=== Stack Trace ===")
            appendLine(sw.toString())
            throwable.cause?.let { cause ->
                appendLine("=== Caused by ===")
                val csw = StringWriter()
                cause.printStackTrace(PrintWriter(csw))
                appendLine(csw.toString())
            }
        }
    }

    private fun writeReport(report: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(Date())
        val fileName = "AmmarBrowser-crash-$timestamp.txt"
        val latestName = "AmmarBrowser-crash-latest.txt"

        // Try Downloads
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (writeToDir(downloadsDir, fileName, report) && writeToDir(downloadsDir, latestName, report)) return

        // Try app-specific external
        val extDir = appContext?.getExternalFilesDir("crash_logs")
        if (extDir != null && writeToDir(extDir, fileName, report) && writeToDir(extDir, latestName, report)) return

        // Try internal
        val intDir = appContext?.filesDir?.let { File(it, "crash_logs") }
        if (intDir != null) {
            writeToDir(intDir, fileName, report)
            writeToDir(intDir, latestName, report)
        }
    }

    private fun writeToDir(dir: File, name: String, content: String): Boolean {
        return try {
            dir.mkdirs()
            File(dir, name).writeText(content)
            true
        } catch (_: Exception) {
            false
        }
    }
}
