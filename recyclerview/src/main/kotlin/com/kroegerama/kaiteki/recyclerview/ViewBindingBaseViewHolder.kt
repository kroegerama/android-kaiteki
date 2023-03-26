package com.kroegerama.kaiteki.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

open class ViewBindingBaseViewHolder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun <VB : ViewBinding> create(
            parent: ViewGroup,
            bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
        ): ViewBindingBaseViewHolder<VB> = ViewBindingBaseViewHolder(
            bindingInflater.invoke(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
}
