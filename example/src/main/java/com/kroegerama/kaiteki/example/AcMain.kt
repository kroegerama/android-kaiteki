package com.kroegerama.kaiteki.example

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.transition.Fade
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kroegerama.kaiteki.PermissionEvent
import com.kroegerama.kaiteki.PermissionHandler
import com.kroegerama.kaiteki.baseui.BaseFragmentActivity
import com.kroegerama.kaiteki.injectTransition
import kotlinx.android.synthetic.main.ac_main.*

class AcMain : BaseFragmentActivity<Navigation>(
    R.layout.ac_main,
    R.id.container,
    Navigation.Main
) {

    private val permissionHandler by lazy {
        PermissionHandler(
            listOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_COARSE_LOCATION),
            1337,
            ::permissionEvent
        )
    }

    override fun setupGUI() {
        setSupportActionBar(toolbar)
        toolbar.injectTransition()

        btnTest.setOnClickListener { testPermissions() }
        bottomNav.setOnNavigationItemSelectedListener(::navigationSelected)
    }

    override fun saveIndexState(index: Navigation, key: String, bundle: Bundle) {
        bundle.putInt(key, index.ordinal)
    }

    override fun loadIndexState(key: String, bundle: Bundle): Navigation? {
        return bundle.getInt(key).let { Navigation.values()[it] }
    }

    private fun navigationSelected(item: MenuItem) = true.also {
        val result = navigator.show(Navigation.fromIdx(item.itemId))
        Log.d(TAG, "navSelect: $result")
    }

    override fun FragmentTransaction.decorate(fromIndex: Navigation?, toIndex: Navigation, fragment: Fragment) {
        setReorderingAllowed(true)
    }

    override fun createFragment(index: Navigation, payload: Any?) = when (index) {
        Navigation.Main,
        Navigation.Info,
        Navigation.Map -> VBFragment.makeInstance(index.idx).apply {
            enterTransition = Fade()
            exitTransition = Fade()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionHandler.onResult(this, requestCode, permissions, grantResults)
    }

    private fun permissionEvent(event: PermissionEvent) {
        val list = event.permissions.joinToString("\n- ", "\n- ")
        MaterialAlertDialogBuilder(this, R.style.Theme_MaterialComponents_Dialog)
            .apply {
                when (event) {
                    is PermissionEvent.PermissionGranted -> {
                        setTitle("Granted :)")
                        setMessage(list)
                        setPositiveButton("Ok", null)
                    }
                    is PermissionEvent.ShowRationale -> {
                        setTitle("Rationale...")
                        setMessage(list)
                        setPositiveButton("Allow") { _, _ -> event.doRequest() }
                        setNegativeButton("Cancel", null)
                    }
                    is PermissionEvent.PermissionDeniedOnce -> {
                        setTitle("Permissions denied :(")
                        setMessage("App won't work correctly\n$list")
                        setPositiveButton("Ok", null)
                        setNeutralButton("Allow") { _, _ -> event.restartRequest() }
                    }
                    is PermissionEvent.PermissionDeniedPermanently -> {
                        setTitle("Permission denied permanently :(")
                        setMessage(
                            "App won't work properly until you allow them.\n" +
                                    "Go to System > Settings > Apps > Properties to enable.\n$list"
                        )
                        setPositiveButton("Ok", null)
                    }
                }
            }
            .show()
    }

    private fun testPermissions() {
        permissionHandler.checkPermissions(this)
    }

    companion object {
        private const val TAG = "AcMain"
    }
}
