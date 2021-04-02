package com.kroegerama.kaiteki.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding

abstract class ViewBindingPagingDataAdapter<T : Any, VB : ViewBinding>(
    protected val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB,
    diffCallback: DiffUtil.ItemCallback<T>,
    @Suppress("MemberVisibilityCanBePrivate")
    protected val rootClickListener: ((item: T?) -> Unit)? = null
) : PagingDataAdapter<T, ViewBindingBaseViewHolder<VB>>(diffCallback) {

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

    protected fun getItemAtPosition(position: Int) = getItem(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewBindingBaseViewHolder<VB> {
        val binding = bindingInflater(LayoutInflater.from(parent.context), parent, false).apply {
            prepare()
        }
        return ViewBindingBaseViewHolder(binding).apply {
            binding.injectListeners(this, viewType) { getItem(bindingAdapterPosition) }
        }
    }

    override fun onBindViewHolder(holder: ViewBindingBaseViewHolder<VB>, position: Int) = with(holder) {
        binding.update(this, itemView.context, itemViewType, getItem(position))
    }

}
