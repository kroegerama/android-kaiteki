package com.kroegerama.kaiteki

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class PermissionHandler(
    private val permissions: List<String>,
    private val requestCode: Int,
    private val onEvent: (PermissionEvent) -> Unit
) {

    fun checkPermissions(activity: Activity, forceRequest: Boolean = false) = checkPermissionsInternal(activity, forceRequest)
    fun checkPermissions(fragment: Fragment, forceRequest: Boolean = false) = checkPermissionsInternal(fragment, forceRequest)

    fun onResult(activity: Activity, requestCode: Int, permissions: Array<out String>, grantResults: IntArray) =
        onResultInternal(activity, requestCode, permissions, grantResults)

    fun onResult(fragment: Fragment, requestCode: Int, permissions: Array<out String>, grantResults: IntArray) =
        onResultInternal(fragment, requestCode, permissions, grantResults)

    private fun checkPermissionsInternal(target: Any, forceRequest: Boolean) {
        val (granted, notGranted) = permissions.partition { permission ->
            target.checkPermission(permission)
        }
        if (granted.isNotEmpty()) {
            onEvent(PermissionEvent.PermissionGranted(granted))
        }
        if (notGranted.isEmpty()) return

        val (rationaleNeeded, requestNeeded) = notGranted.partition { permission ->
            !forceRequest && target.shouldShowRationale(permission)
        }
        if (rationaleNeeded.isNotEmpty()) {
            onEvent(PermissionEvent.ShowRationale(rationaleNeeded) {
                target.doRequest(rationaleNeeded)
            })
        }
        if (requestNeeded.isNotEmpty()) {
            target.doRequest(requestNeeded)
        }
    }


    private fun onResultInternal(target: Any, requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != this.requestCode) return

        val (granted, notGranted) = permissions.zip(grantResults.toTypedArray()).partition {
            it.second.isPermissionGranted()
        }
        if (granted.isNotEmpty()) {
            onEvent(PermissionEvent.PermissionGranted(granted.map { it.first }))
        }
        if (notGranted.isEmpty()) return

        val (deniedOnce, deniedPermanently) = notGranted.map { it.first }.partition { permission ->
            target.shouldShowRationale(permission)
        }

        if (deniedOnce.isNotEmpty()) {
            onEvent(PermissionEvent.PermissionDeniedOnce(deniedOnce) {
                target.doRequest(deniedOnce)
            })
        }
        if (deniedPermanently.isNotEmpty()) {
            onEvent(PermissionEvent.PermissionDeniedPermanently(deniedPermanently))
        }
    }

    private fun Any.doRequest(permissions: List<String>): Unit = when (this) {
        is Activity -> ActivityCompat.requestPermissions(this, permissions.toTypedArray(), requestCode)
        is Fragment -> this.requestPermissions(permissions.toTypedArray(), requestCode)
        else -> throw IllegalArgumentException()
    }

    private fun Any.shouldShowRationale(permission: String): Boolean = when (this) {
        is Activity -> ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
        is Fragment -> this.shouldShowRequestPermissionRationale(permission)
        else -> throw IllegalArgumentException()
    }

    private fun Any.checkPermission(permission: String): Boolean = when (this) {
        is Context -> ContextCompat.checkSelfPermission(this, permission).isPermissionGranted()
        is Fragment -> requireContext().checkPermission(permission)
        else -> throw IllegalArgumentException()
    }

    private fun Int.isPermissionGranted() = this == PackageManager.PERMISSION_GRANTED
}

sealed class PermissionEvent(
    open val permissions: List<String>
) {
    val isGranted get() = this is PermissionGranted

    /**
     * Permission was ganted
     */
    data class PermissionGranted(
        override val permissions: List<String>
    ) : PermissionEvent(permissions)

    /**
     * Permission was denied previously -> Show rationale, then invoke doRequest()
     */
    data class ShowRationale(
        override val permissions: List<String>,
        val doRequest: () -> Unit
    ) : PermissionEvent(permissions)

    /**
     * Permission was denied right now -> Continue without the permission. May show a dialog about why some functions won't work.
     */
    data class PermissionDeniedOnce(
        override val permissions: List<String>,
        val restartRequest: () -> Unit
    ) : PermissionEvent(permissions)

    /**
     * Permission was denied permanently -> Continue
     */
    data class PermissionDeniedPermanently(
        override val permissions: List<String>
    ) : PermissionEvent(permissions)
}