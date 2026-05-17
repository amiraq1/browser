package com.ammar.browser.permissions

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Centralized permission management for the browser.
 * Expandable for camera, location, storage, etc.
 */
object PermissionManager {

    const val REQUEST_CODE_PERMISSIONS = 1001

    fun hasPermission(activity: Activity, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions(activity: Activity, permissions: Array<String>) {
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE_PERMISSIONS)
    }
}
