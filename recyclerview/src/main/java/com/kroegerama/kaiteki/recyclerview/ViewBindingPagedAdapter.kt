package com.kroegerama.kaiteki.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding

abstract class ViewBindingPagedAdapter<T, VB : ViewBinding>(
    protected val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB,
    diffCallback: DiffUtil.ItemCallback<T>,
    protected val rootClickListener: ((item: T?) -> Unit)? = null
) : PagedListAdapter<T, ViewBindingBaseViewHolder<T?, VB>>(
    diffCallback
) {

    abstract fun VB.update(
        viewHolder: ViewBindingBaseViewHolder<T?, VB>,
        context: Context,
        viewType: Int,
        item: T?
    )

    open fun VB.injectListeners(
        viewHolder: ViewBindingBaseViewHolder<T?, VB>,
        viewType: Int,
        currentItem: () -> T?
    ) {
        rootClickListener?.let { root.setOnClickListener { it(currentItem()) } }
    }

    protected open fun VB.prepare() = Unit

    protected fun getItemAtPosition(position: Int) = getItem(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewBindingBaseViewHolder<T?, VB> {
        val binding = bindingInflater(LayoutInflater.from(parent.context), parent, false)
        return ViewBindingBaseViewHolder(
            binding,
            { viewHolder, item -> binding.update(viewHolder, viewHolder.itemView.context, viewType, item) }
        ) { viewHolder, itemGetter ->
            prepare()
            injectListeners(viewHolder, viewType, itemGetter)
        }
    }

    override fun onBindViewHolder(holder: ViewBindingBaseViewHolder<T?, VB>, position: Int) =
        holder.update(getItem(position))

}

fun <T> createDiffItemCallback(
    compareItems: (checkContent: Boolean, a: T, b: T) -> Boolean
) = object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T) =
        compareItems(false, oldItem, newItem)

    override fun areContentsTheSame(oldItem: T, newItem: T) =
        compareItems(true, oldItem, newItem)
}