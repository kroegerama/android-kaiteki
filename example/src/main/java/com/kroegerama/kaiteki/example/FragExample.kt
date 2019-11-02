package com.kroegerama.kaiteki.example

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.ColorInt
import androidx.core.view.MenuItemCompat
import androidx.core.view.updatePadding
import com.google.android.material.animation.ArgbEvaluatorCompat
import com.kroegerama.kaiteki.baseui.BaseFragment
import com.kroegerama.kaiteki.bundle
import com.kroegerama.kaiteki.get
import kotlinx.android.synthetic.main.frag_example.*

class FragExample : BaseFragment(
    layout = R.layout.frag_example,
    optionsMenu = R.menu.mnu_example
) {

    private var test = false

    private var argIndex: Int = -1

    override fun handleArguments(args: Bundle) {
        argIndex = args.getInt(ARG_INDEX, -1)
    }

    override fun saveState(outState: Bundle) {
        outState.putBoolean("b", test)
    }

    override fun loadState(state: Bundle) {
        test = state.getBoolean("b")
        btnTest.text = test.toString()
    }

    override fun setupGUI() {
        btnTest.text = test.toString()
        btnTest.setOnClickListener {
            test = !test
            btnTest.text = test.toString()
        }

        tvTest.text = argIndex.toString()

        val pad = when (argIndex) {
            R.id.navMain -> 100
            R.id.navInfo -> 200
            R.id.navMap -> 300
            else -> 400
        }
        view?.updatePadding(top = pad)
    }

    private fun MenuItem.animateTint(show: Boolean, @ColorInt colorVisible: Int, @ColorInt colorInvisible: Int) {
        if (isVisible == show) return

        val from = if (show) colorInvisible else colorVisible
        val to = if (show) colorVisible else colorInvisible

        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 400
            addUpdateListener {
                val value = it.animatedValue as Float
                val c = ArgbEvaluatorCompat.getInstance().evaluate(value, from, to)
                MenuItemCompat.setIconTintList(this@animateTint, ColorStateList.valueOf(c))
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    isVisible = show
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    isVisible = true
                }

            })
        }.start()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu[R.id.mnuFace]?.let {
            it.isVisible = argIndex == R.id.navMain
//            it.animateTint(visible, Color.WHITE, Color.TRANSPARENT)
        }
        menu[R.id.mnuFlower]?.let {
            it.isVisible = argIndex in listOf(R.id.navMain, R.id.navMap)
//            it.animateTint(visible, Color.WHITE, Color.TRANSPARENT)
        }
    }

    companion object {
        private const val TAG = "FragExample"

        private const val ARG_INDEX = "index"

        fun makeInstance(index: Int) = FragExample().apply {
            Log.d(TAG, "Make $index")
            arguments = bundle {
                putInt(ARG_INDEX, index)
            }
        }
    }
}