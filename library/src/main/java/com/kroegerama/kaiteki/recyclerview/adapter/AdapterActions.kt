package com.kroegerama.kaiteki.recyclerview.adapter

interface AdapterActions<T> {
    fun addItem(item: T)

    fun addAll(items: Collection<T>)

    fun setItems(items: Collection<T>)

    fun getItems(): Collection<T>

    fun getItem(position: Int): T

    fun removeItem(position: Int): T

    fun insertItem(position: Int, item: T)

    fun moveItem(fromPosition: Int, toPosition: Int): Boolean

    fun clear()
}