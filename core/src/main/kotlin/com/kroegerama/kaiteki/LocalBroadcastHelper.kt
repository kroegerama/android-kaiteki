package com.kroegerama.kaiteki

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager

abstract class LocalBroadcastHelper(
    private val applicationContext: Context
) {

    private val localBroadcastManager get() = LocalBroadcastManager.getInstance(applicationContext)

    protected fun sendBroadcast(action: String, block: Intent.() -> Unit = {}) =
        localBroadcastManager.sendBroadcast(Intent(action).also(block))

    protected fun registerReceiverForLifecycle(
        lifecycleOwner: LifecycleOwner,
        action: String,
        callback: Intent.() -> Unit
    ) {
        lifecycleOwner.lifecycle.addObserver(object : BroadcastReceiver(), DefaultLifecycleObserver {
            override fun onReceive(context: Context, intent: Intent) {
                callback(intent)
            }

            override fun onCreate(owner: LifecycleOwner) {
                localBroadcastManager.registerReceiver(this, IntentFilter(action))
            }

            override fun onDestroy(owner: LifecycleOwner) {
                localBroadcastManager.unregisterReceiver(this)
            }
        })
    }

}
