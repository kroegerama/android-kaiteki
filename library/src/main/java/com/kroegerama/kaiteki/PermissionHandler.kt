package com.kroegerama.kaiteki

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionHandler(
        private val context: Activity,
        private val permission: String,
        private val requestKey: Int = System.identityHashCode(permission) and 0xffff,
        private val onGranted: () -> Unit,
        private val onNotGranted: (() -> Unit)? = null
) {
    fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                        context,
                        permission
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                request()
            } else {
                onNotGranted?.invoke()
            }
        } else {
            onGranted.invoke()
        }
    }

    fun onResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray): Boolean {
        return when (requestCode) {
            requestKey -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    onGranted.invoke()
                } else {
                    onNotGranted?.invoke()
                }
                true
            }
            else -> {
                false
            }
        }
    }

    private fun request() {
        ActivityCompat.requestPermissions(
                context,
                arrayOf(permission),
                requestKey
        )
    }
}