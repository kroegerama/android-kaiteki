package com.kroegerama.kaiteki.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

abstract class ViewBindingListAdapter<T, VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB,
    diffCallback: DiffUtil.ItemCallback<T>,
    protected val rootClickListener: ((item: T?) -> Unit)? = null
) : ListAdapter<T, ViewBindingBaseViewHolder<VB>>(diffCallback) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    open suspend fun VB.update(
        viewHolder: ViewBindingBaseViewHolder<VB>,
        context: Context,
        viewType: Int,
        item: T?
    ) = Unit

    open fun VB.injectListeners(
        viewHolder: ViewBindingBaseViewHolder<VB>,
        viewType: Int,
        currentItem: () -> T?
    ) {
        rootClickListener?.let { root.setOnClickListener { it(currentItem()) } }
    }

    protected open fun VB.prepare() = Unit

    @CallSuper
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewBindingBaseViewHolder.create(parent, bindingInflater).apply {
        binding.prepare()
        binding.injectListeners(this, viewType) { getCurrentItem() }
    }

    @CallSuper
    override fun onBindViewHolder(holder: ViewBindingBaseViewHolder<VB>, position: Int) {
        val job = holder.itemView.getTag(R.id.viewBindingViewHolderCurrentJob) as? Job
        job?.cancel()
        holder.itemView.setTag(
            R.id.viewBindingViewHolderCurrentJob,
            scope.launch {
                holder.binding.update(holder, holder.itemView.context, holder.itemViewType, getItem(position))
                holder.itemView.setTag(R.id.viewBindingViewHolderCurrentJob, null)
            }
        )
    }

    @CallSuper
    override fun onViewRecycled(holder: ViewBindingBaseViewHolder<VB>) {
        val job = holder.itemView.getTag(R.id.viewBindingViewHolderCurrentJob) as? Job
        job?.cancel()
    }

    protected fun RecyclerView.ViewHolder.getCurrentItem(): T? =
        bindingAdapterPosition.takeUnless { it == RecyclerView.NO_POSITION }?.let(::getItem)

}
