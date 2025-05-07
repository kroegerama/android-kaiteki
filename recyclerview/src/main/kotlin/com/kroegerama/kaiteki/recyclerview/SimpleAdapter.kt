package com.kroegerama.kaiteki.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class SimpleAdapter<T>(
    private val itemLayoutRes: Int
) : BaseAdapter<T>() {

    abstract fun View.simpleUpdate(viewHolder: BaseViewHolder<T>, item: T?)
    abstract fun itemClick(viewHolder: BaseViewHolder<T>, item: T?)

    final override fun getLayoutRes(viewType: Int) = itemLayoutRes
    override fun injectListeners(
        viewHolder: BaseViewHolder<T>,
        viewType: Int,
        currentItem: () -> T?
    ) = with(viewHolder) {
        itemView.setOnClickListener { itemClick(viewHolder, currentItem()) }
    }

    override fun updater(viewHolder: BaseViewHolder<T>, viewType: Int, item: T?) =
        viewHolder.itemView.simpleUpdate(viewHolder, item)
}

abstract class BaseAdapter<T> : RecyclerView.Adapter<BaseViewHolder<T>>() {

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
        val count = items.size
        if (count == 0) {
            return
        }
        items.clear()
        notifyItemRangeRemoved(0, count)
    }

    fun getItems(): List<T> = ArrayList(items)

    abstract fun getLayoutRes(viewType: Int): Int
    abstract fun updater(viewHolder: BaseViewHolder<T>, viewType: Int, item: T?)
    abstract fun injectListeners(
        viewHolder: BaseViewHolder<T>,
        viewType: Int,
        currentItem: () -> T?
    )

    protected open fun prepare() = Unit

    abstract fun compareItems(checkContent: Boolean, a: T, b: T): Boolean

    protected fun getItemAtPosition(position: Int) = items[position]

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> =
        BaseViewHolder(
            parent,
            getLayoutRes(viewType),
            viewType,
            ::updater
        ) { viewHolder, itemGetter ->
            prepare()
            injectListeners(viewHolder, viewType, itemGetter)
        }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) =
        holder.update(items[position])

}

class BaseViewHolder<T>(
    parent: ViewGroup,
    @LayoutRes layout: Int,
    private val viewType: Int,
    private val updater: BaseViewHolder<T>.(viewType: Int, item: T?) -> Unit,
    injectListeners: (BaseViewHolder<T>, () -> T?) -> Unit
) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false)) {

    private var currentItem: T? = null

    init {
        injectListeners(this) { currentItem }
    }

    fun update(item: T) {
        currentItem = item
        updater(this, viewType, item)
    }

}

fun <T> calculateDiff(
    oldItems: List<T>,
    newItems: List<T>,
    sameItems: (checkContent: Boolean, a: T, b: T) -> Boolean
) = DiffUtil.calculateDiff(createSimpleDiff(oldItems, newItems, sameItems))

fun <T> createSimpleDiff(
    oldItems: List<T>,
    newItems: List<T>,
    sameItems: (checkContent: Boolean, a: T, b: T) -> Boolean
) = object : DiffUtil.Callback() {
    override fun getOldListSize() = oldItems.size
    override fun getNewListSize() = newItems.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        sameItems(false, oldItems[oldItemPosition], newItems[newItemPosition])

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        sameItems(true, oldItems[oldItemPosition], newItems[newItemPosition])
}