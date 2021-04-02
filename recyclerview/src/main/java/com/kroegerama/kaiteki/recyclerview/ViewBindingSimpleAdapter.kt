package com.kroegerama.kaiteki.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class ViewBindingBaseAdapter<T, VB : ViewBinding>(
    protected val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB,
    private val items: MutableList<T> = ArrayList(),
    protected val rootClickListener: ((item: T?) -> Unit)? = null
) : RecyclerView.Adapter<ViewBindingBaseViewHolder<VB>>(),
    MutableList<T> by items {

    fun setItems(newItems: List<T>): Unit = delegateAndDiff {
        clear()
        addAll(newItems)
    }

    private fun <E> delegateAndDiff(block: MutableList<T>.() -> E): E {
        val old = ArrayList(items)
        val result = items.block()
        calculateDiff(old, items, ::compareItems).dispatchUpdatesTo(this)
        return result
    }

    override fun add(element: T): Boolean = delegateAndDiff { add(element) }
    override fun remove(element: T): Boolean = delegateAndDiff { remove(element) }
    override fun addAll(elements: Collection<T>): Boolean = delegateAndDiff { addAll(elements) }
    override fun addAll(index: Int, elements: Collection<T>): Boolean = delegateAndDiff { addAll(index, elements) }
    override fun removeAll(elements: Collection<T>): Boolean = delegateAndDiff { removeAll(elements) }
    override fun retainAll(elements: Collection<T>): Boolean = delegateAndDiff { retainAll(elements) }
    override fun clear() = delegateAndDiff { clear() }
    override fun set(index: Int, element: T): T = delegateAndDiff { set(index, element) }
    override fun add(index: Int, element: T) = delegateAndDiff { add(index, element) }
    override fun removeAt(index: Int): T = delegateAndDiff { removeAt(index) }

    protected abstract fun VB.update(
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

    abstract fun compareItems(checkContent: Boolean, a: T, b: T): Boolean

    protected fun getItemAtPosition(position: Int) = items[position]

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewBindingBaseViewHolder<VB> {
        val binding = bindingInflater(LayoutInflater.from(parent.context), parent, false).apply {
            prepare()
        }
        return ViewBindingBaseViewHolder(binding).apply {
            binding.injectListeners(this, viewType) { getItemAtPosition(bindingAdapterPosition) }
        }
    }

    override fun onBindViewHolder(holder: ViewBindingBaseViewHolder<VB>, position: Int) {
        with(holder) {
            binding.update(this, itemView.context, itemViewType, items[position])
        }
    }

}

class ViewBindingBaseViewHolder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)
