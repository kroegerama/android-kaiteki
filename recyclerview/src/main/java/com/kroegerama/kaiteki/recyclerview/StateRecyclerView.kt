package com.kroegerama.kaiteki.recyclerview

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView


class StateRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var layoutManagerState: Parcelable? = null

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable(STATE_SUPER, super.onSaveInstanceState())
        bundle.putParcelable(STATE_LAYOUT, layoutManager?.onSaveInstanceState())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        var superstate: Parcelable? = state
        if (state is Bundle) {
            layoutManagerState = state.getParcelable(STATE_LAYOUT)
            superstate = state.getParcelable(STATE_SUPER)
        }
        super.onRestoreInstanceState(superstate)
    }

    private fun restorePosition() {
        if (layoutManagerState != null) {
            layoutManager?.onRestoreInstanceState(layoutManagerState)
            layoutManagerState = null
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        restorePosition()
    }

    companion object {
        private const val STATE_SUPER = "super"
        private const val STATE_LAYOUT = "layout_manager"

    }
}