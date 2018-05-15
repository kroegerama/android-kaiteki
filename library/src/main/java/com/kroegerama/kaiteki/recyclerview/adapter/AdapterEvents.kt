package com.kroegerama.kaiteki.recyclerview.adapter

import android.view.View

interface AdapterEvents<in T> {
    fun onItemClick(item: T, view: View, position: Int)

    fun onItemLongClick(item: T, view: View, position: Int): Boolean
}