package com.kroegerama.kaiteki.example

import android.content.Context
import com.kroegerama.kaiteki.example.databinding.ListItemBinding
import com.kroegerama.kaiteki.recyclerview.ViewBindingBaseAdapter
import com.kroegerama.kaiteki.recyclerview.ViewBindingBaseViewHolder

class VBAdapter : ViewBindingBaseAdapter<String, ListItemBinding>(ListItemBinding::inflate) {
    override fun ListItemBinding.update(
        viewHolder: ViewBindingBaseViewHolder<String, ListItemBinding>,
        context: Context,
        viewType: Int,
        item: String?
    ) {
        edTest.text = item ?: context.getString(android.R.string.untitled)
    }

    override fun compareItems(checkContent: Boolean, a: String, b: String) = a == b
}