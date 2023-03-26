package com.kroegerama.kaiteki.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

abstract class SingleItemAdapter<T, VB : ViewBinding>(
    private val singleItemInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : RecyclerView.Adapter<ViewBindingBaseViewHolder<VB>>() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    var value: T? = null
        set(value) {
            if (field == null && value == null) return

            val oldVisible = field != null && visible
            field = value
            val newVisible = field != null && visible
            doNotify(oldVisible, newVisible)
        }

    var visible: Boolean = true
        set(value) {
            if (field == value) return
            val oldVisible = field && this.value != null
            field = value
            val newVisible = field && this.value != null
            doNotify(oldVisible, newVisible)
        }

    private fun doNotify(oldVisible: Boolean, newVisible: Boolean) {
        when {
            !oldVisible && newVisible -> notifyItemInserted(0)
            oldVisible && !newVisible -> notifyItemRemoved(0)
            oldVisible && newVisible -> notifyItemChanged(0)
        }
    }

    @CallSuper
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewBindingBaseViewHolder<VB> =
        ViewBindingBaseViewHolder.create(parent, singleItemInflater)

    @CallSuper
    override fun onBindViewHolder(holder: ViewBindingBaseViewHolder<VB>, position: Int) {
        val job = holder.itemView.getTag(R.id.viewBindingViewHolderCurrentJob) as? Job
        job?.cancel()
        holder.itemView.setTag(
            R.id.viewBindingViewHolderCurrentJob,
            scope.launch {
                holder.binding.update(value ?: return@launch)
                holder.itemView.setTag(R.id.viewBindingViewHolderCurrentJob, null)
            }
        )
    }

    @CallSuper
    override fun onViewRecycled(holder: ViewBindingBaseViewHolder<VB>) {
        val job = holder.itemView.getTag(R.id.viewBindingViewHolderCurrentJob) as? Job
        job?.cancel()
    }

    open suspend fun VB.update(value: T) {}

    override fun getItemCount(): Int = if (value != null && visible) 1 else 0
}
