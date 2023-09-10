package com.kroegerama.kaiteki.example

import android.content.Context
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.kroegerama.kaiteki.architecture.launchAndCollectWithLifecycleState
import com.kroegerama.kaiteki.baseui.ViewBindingActivity
import com.kroegerama.kaiteki.example.databinding.AcMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VBActivity : ViewBindingActivity<AcMainBinding>(AcMainBinding::inflate) {

    val viewModel: TestViewModel by viewModels()
    val prefs by lazy { PrefController(getPreferences(Context.MODE_PRIVATE)) }

    override fun AcMainBinding.setupGUI() {
        btnTest.setOnClickListener {
            VBMaterialDialog.show(supportFragmentManager)
            prefs.myNewLong = (prefs.myNewLong ?: 0) + 1
            prefs.myNewInt = (prefs.myNewInt ?: 0) + 1
            prefs.myNewString = (prefs.myNewString ?: "") + prefs.myNewString?.length
            prefs.myNewStringSet = (prefs.myNewStringSet ?: mutableSetOf()) + prefs.myNewStringSet?.size.toString()
            prefs.myNewBoolean = !(prefs.myNewBoolean ?: false)
            prefs.myNewFloat = (prefs.myNewFloat ?: 0f) + .1f
            prefs.myNewEnum = (prefs.myNewEnum ?: PrefController.TestEnum.One).next
        }

        Log.d(TAG, prefs.myNewLong.toString())
        Log.d(TAG, prefs.myNewInt.toString())
        Log.d(TAG, prefs.myNewString.toString())
        Log.d(TAG, prefs.myNewStringSet.toString())
        Log.d(TAG, prefs.myNewBoolean.toString())
        Log.d(TAG, prefs.myNewFloat.toString())
        Log.d(TAG, prefs.myNewEnum.toString())

        prefs.intFlow.launchAndCollectWithLifecycleState(this@VBActivity) {
            Log.d(TAG, ">>> $it")
        }

        lifecycleScope.launch {
            delay(1000)
            prefs.myNewInt = 10
            delay(500)
            prefs.myNewInt = 0
            delay(500)
            prefs.myNewInt = null
            delay(500)
            prefs.myNewInt = 5
        }
    }

    companion object {
        private const val TAG = "VBActivity"
    }

}