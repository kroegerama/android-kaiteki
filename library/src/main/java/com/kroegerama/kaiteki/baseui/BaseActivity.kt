package com.kroegerama.kaiteki.baseui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    private var runState: RunState = RunState.NORMAL_START

    protected val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    protected val isFirstRun: Boolean
        get() = getPreferences(Context.MODE_PRIVATE).getBoolean(PREF_FIRST_RUN, true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepare()
        setContentView(layoutResource)
        setupGUI()

        loadPreferences(preferences)
        if (intent.extras != null) {
            handleArguments(intent.extras)
        }
        if (savedInstanceState != null) {
            loadState(savedInstanceState)
        }

        runState = when (savedInstanceState) {
            null -> if (isFirstRun) RunState.FIRST_START else RunState.NORMAL_START
            else -> RunState.RESUMED_START
        }

        run(runState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveState(outState)
    }

    override fun onPause() {
        super.onPause()
        if (isFirstRun) {
            getPreferences(Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(PREF_FIRST_RUN, false)
                    .apply()
        }
        savePreferences(preferences)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu) ||
                optionsMenuResource.let {
                    return@let if (it > 0) {
                        menuInflater.inflate(it, menu)
                        true
                    } else false
                }

    }

    protected abstract val layoutResource: Int

    protected open val optionsMenuResource: Int = 0

    protected open fun prepare() {}

    protected open fun setupGUI() {}

    protected open fun handleArguments(args: Bundle) {}

    protected open fun loadPreferences(prefs: SharedPreferences) {}

    protected open fun loadState(state: Bundle) {}

    protected open fun saveState(outState: Bundle) {}

    protected open fun savePreferences(outPrefs: SharedPreferences) {}

    protected open fun run(runState: RunState) {}

    enum class RunState {
        FIRST_START, NORMAL_START, RESUMED_START
    }

    companion object {
        private const val PREF_FIRST_RUN = "first_run"
    }
}