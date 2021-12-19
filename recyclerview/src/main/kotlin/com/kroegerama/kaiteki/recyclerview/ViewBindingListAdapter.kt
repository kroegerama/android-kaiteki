package com.kroegerama.kaiteki.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding

abstract class ViewBindingListAdapter<T, VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB,
    diffCallback: DiffUtil.ItemCallback<T>,
    protected val rootClickListener: ((item: T?) -> Unit)? = null
) : ListAdapter<T, ViewBindingBaseViewHolder<VB>>(diffCallback) {

    abstract fun VB.update(
        viewHolder: ViewBindingBaseViewHolder<VB>,
        context: Context,
        viewType: Int,
        item: T?
    )

    open fun VB.injectListeners(
        viewHolder: ViewBindingBaseViewHolder<VB>,
        viewType: Int,
        currentItem: () -> T?
    ) {
        rootClickListener?.let { root.setOnClickListener { it(currentItem()) } }
    }

    protected open fun VB.prepare() = Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewBindingBaseViewHolder.create(parent, bindingInflater).apply {
        binding.prepare()
        binding.injectListeners(this, viewType) { getItem(bindingAdapterPosition) }
    }

    override fun onBindViewHolder(holder: ViewBindingBaseViewHolder<VB>, position: Int) = with(holder) {
        binding.update(this, itemView.context, itemViewType, getItem(position))
    }

}
