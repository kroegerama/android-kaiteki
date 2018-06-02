package com.kroegerama.kaiteki.recyclerview.adapter

import android.content.Context


abstract class BaseItemAdapter<T, VH : BaseViewHolder<T>>(
        context: Context,
        listener: AdapterEvents<T>? = null,
        initialItems: Collection<T>? = null
) : BaseAdapter<T, VH>(context, listener), AdapterActions<T> {

    private val items: MutableList<T> = if (initialItems != null) {
        ArrayList(initialItems)
    } else {
        ArrayList()
    }

    init {
        itemProvider = object : ItemProvider<T> {
            override fun count(): Int {
                return items.size
            }

            override fun get(position: Int): T {
                return items[position]
            }
        }
    }

    override fun addItem(item: T) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    override fun addAll(items: Collection<T>) {
        this.items.addAll(items)
        notifyItemRangeInserted(this.items.size - items.size, items.size)
    }

    override fun setItems(items: Collection<T>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun getItems(): Collection<T> {
        return items
    }

    override fun getItem(position: Int): T {
        return items[position]
    }

    override fun removeItem(position: Int): T {
        val result = items.removeAt(position)
        notifyItemRemoved(position)
        return result
    }

    fun removeItem(item: T): Boolean {
        val idx = items.indexOf(item)
        val result = items.remove(item)
        if (result) {
            notifyItemRemoved(idx)
        }
        return result
    }

    override fun insertItem(position: Int, item: T) {
        items.add(position, item)
        notifyItemInserted(position)
    }

    override fun moveItem(fromPosition: Int, toPosition: Int): Boolean {
        if (fromPosition == toPosition) {
            return false
        }
        val item = items.removeAt(fromPosition)
        items.add(toPosition, item)
        return true
    }

    override fun clear() {
        val size = items.size
        items.clear()
        notifyItemRangeRemoved(0, size)
    }
}