package com.kroegerama.kaiteki.recyclerview

import androidx.recyclerview.widget.DiffUtil

fun <T : Any> createDiffItemCallback(
    compareItems: (checkContent: Boolean, a: T, b: T) -> Boolean
) = object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T) =
        compareItems(false, oldItem, newItem)

    override fun areContentsTheSame(oldItem: T, newItem: T) =
        compareItems(true, oldItem, newItem)
}

fun <T : Any> createDefaultDiffCallback(idGetter: (T) -> Any?) = createDiffItemCallback<T> { checkContent, a, b ->
    if (checkContent) a == b else idGetter(a) == idGetter(b)
}
