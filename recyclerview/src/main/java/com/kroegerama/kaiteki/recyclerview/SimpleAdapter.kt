package com.kroegerama.kaiteki.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class SimpleAdapter<T>(
    private val itemLayoutRes: Int
) : BaseAdapter<T>() {

    abstract fun simpleUpdate(viewHolder: BaseViewHolder<T>, item: T?)
    abstract fun itemClick(viewHolder: BaseViewHolder<T>, item: T?)

    override fun getLayoutRes(viewType: Int) = itemLayoutRes
    override fun injectListeners(
        viewHolder: BaseViewHolder<T>,
        viewType: Int,
        currentItem: () -> T?
    ) = with(viewHolder) {
        itemView.setOnClickListener { itemClick(viewHolder, currentItem()) }
    }

    override fun updater(viewHolder: BaseViewHolder<T>, viewType: Int, item: T?) =
        simpleUpdate(viewHolder, item)
}

abstract class BaseAdapter<T> : RecyclerView.Adapter<BaseViewHolder<T>>() {

    private val items = ArrayList<T>()

    abstract fun getLayoutRes(viewType: Int): Int
    abstract fun updater(viewHolder: BaseViewHolder<T>, viewType: Int, item: T?)
    abstract fun injectListeners(
        viewHolder: BaseViewHolder<T>,
        viewType: Int,
        currentItem: () -> T?
    )

    abstract fun compareItems(checkContent: Boolean, a: T, b: T): Boolean

    fun setItems(newItems: List<T>) {
        val diff = calculateDiff(items, newItems, ::compareItems)
        items.clear()
        items.addAll(newItems)
        diff.dispatchUpdatesTo(this)
    }

    protected fun getItemAtPosition(position: Int) = items[position]

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> =
        BaseViewHolder(
            parent,
            getLayoutRes(viewType),
            viewType,
            ::updater
        ) { viewHolder, itemGetter ->
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