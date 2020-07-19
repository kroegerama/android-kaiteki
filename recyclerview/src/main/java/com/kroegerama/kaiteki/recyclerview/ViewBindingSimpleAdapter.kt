package com.kroegerama.kaiteki.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class ViewBindingBaseAdapter<T, VB : ViewBinding>(
    protected val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB,
    protected val rootClickListener: ((item: T?) -> Unit)? = null
) : RecyclerView.Adapter<ViewBindingBaseViewHolder<T, VB>>() {

    private val items = ArrayList<T>()

    fun setItems(newItems: List<T>) {
        val diff = calculateDiff(items, newItems, ::compareItems)
        items.clear()
        items.addAll(newItems)
        diff.dispatchUpdatesTo(this)
    }

    fun add(element: T) {
        items.add(element)
        notifyItemInserted(itemCount - 1)
    }

    fun add(index: Int, element: T) {
        items.add(index, element)
        notifyItemInserted(index)
    }

    fun set(index: Int, element: T) {
        items[index] = element
        notifyItemChanged(index)
    }

    fun remove(element: T) {
        val idx = items.indexOf(element)
        if (idx >= 0) {
            items.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }

    fun removeAt(index: Int) {
        items.removeAt(index)
        notifyItemRemoved(index)
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    fun getItems(): List<T> = ArrayList(items)

    abstract fun VB.update(
        viewHolder: ViewBindingBaseViewHolder<T, VB>,
        context: Context,
        viewType: Int,
        item: T?
    )

    open fun VB.injectListeners(
        viewHolder: ViewBindingBaseViewHolder<T, VB>,
        viewType: Int,
        currentItem: () -> T?
    ) {
        rootClickListener?.let { root.setOnClickListener { it(currentItem()) } }
    }

    protected open fun VB.prepare() = Unit

    abstract fun compareItems(checkContent: Boolean, a: T, b: T): Boolean

    protected fun getItemAtPosition(position: Int) = items[position]

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewBindingBaseViewHolder<T, VB> {
        val binding = bindingInflater(LayoutInflater.from(parent.context), parent, false)
        return ViewBindingBaseViewHolder(
            binding,
            { viewHolder, item -> binding.update(viewHolder, viewHolder.itemView.context, viewType, item) }
        ) { viewHolder, itemGetter ->
            prepare()
            injectListeners(viewHolder, viewType, itemGetter)
        }
    }

    override fun onBindViewHolder(holder: ViewBindingBaseViewHolder<T, VB>, position: Int) =
        holder.update(items[position])

}

class ViewBindingBaseViewHolder<T, VB : ViewBinding>(
    private val binding: VB,
    private val updater: VB.(viewHolder: ViewBindingBaseViewHolder<T, VB>, item: T?) -> Unit,
    injectListeners: VB.(ViewBindingBaseViewHolder<T, VB>, () -> T?) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private var currentItem: T? = null

    init {
        binding.injectListeners(this) { currentItem }
    }

    fun update(item: T) {
        currentItem = item
        binding.updater(this, item)
    }

}