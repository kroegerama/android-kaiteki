package com.kroegerama.kaiteki.recyclerview.adapter

import android.content.Context
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

abstract class BaseAdapter<T, VH : BaseViewHolder<T>>(
        protected val context: Context,
        protected var listener: AdapterEvents<T>? = null,
        protected var itemProvider: ItemProvider<T>? = null
) : RecyclerView.Adapter<VH>() {

    var clickable: Boolean = false
        get() = field && listener != null
        set(clickable) {
            field = clickable
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layoutRes = getLayout(viewType)
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return createViewHolder(view, parent.context, viewType)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        itemProvider?.let {
            val item = it[position]
            holder.update(item, position)
            if (clickable) {
                for (v in holder.clickableViews.orEmpty()) {
                    v.setOnClickListener { view ->
                        listener?.onItemClick(item, view, position)
                    }
                    v.setOnLongClickListener { view ->
                        listener?.onItemLongClick(item, view, position) == true
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = itemProvider?.count() ?: 0

    protected abstract fun createViewHolder(view: View, context: Context, viewType: Int): VH

    @LayoutRes
    protected abstract fun getLayout(viewType: Int): Int
}
