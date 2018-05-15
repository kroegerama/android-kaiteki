package com.kroegerama.kaiteki.recyclerview.adapter

interface ItemProvider<out T> {
    fun count(): Int

    operator fun get(position: Int): T
}