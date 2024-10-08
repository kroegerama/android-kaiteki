package com.kroegerama.kaiteki.paging

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.kroegerama.kaiteki.recyclerview.ViewBindingBaseViewHolder

abstract class ViewBindingPagingDataAdapter<T : Any, VB : ViewBinding>(
    @Suppress("MemberVisibilityCanBePrivate")
    protected val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB,
    diffCallback: DiffUtil.ItemCallback<T>,
    @Suppress("MemberVisibilityCanBePrivate")
    protected val rootClickListener: ((item: T?) -> Unit)? = null
) : PagingDataAdapter<T, ViewBindingBaseViewHolder<VB>>(diffCallback) {

    open fun VB.update(
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

    protected fun getItemAtPosition(position: Int) = getItem(position)

    @CallSuper
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewBindingBaseViewHolder.create(parent, bindingInflater).apply {
        binding.prepare()
        binding.injectListeners(this, viewType) { getCurrentItem() }
    }

    @CallSuper
    override fun onBindViewHolder(holder: ViewBindingBaseViewHolder<VB>, position: Int) {
        holder.binding.update(holder, holder.itemView.context, holder.itemViewType, getItem(position))
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun RecyclerView.ViewHolder.getCurrentItem(): T? =
        bindingAdapterPosition.takeUnless { it == RecyclerView.NO_POSITION }?.let(::getItem)

}
