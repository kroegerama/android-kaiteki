package com.kroegerama.kaiteki.example

import android.content.Context
import com.kroegerama.kaiteki.example.databinding.ListItemBinding
import com.kroegerama.kaiteki.recyclerview.ViewBindingBaseViewHolder
import com.kroegerama.kaiteki.recyclerview.ViewBindingListAdapter
import com.kroegerama.kaiteki.recyclerview.createDefaultDiffCallback

class VBAdapter : ViewBindingListAdapter<String, ListItemBinding>(
    ListItemBinding::inflate,
    diffCallback = createDefaultDiffCallback { it }
) {
    override fun ListItemBinding.update(
        viewHolder: ViewBindingBaseViewHolder<ListItemBinding>,
        context: Context,
        viewType: Int,
        item: String?
    ) {
        edTest.text = item ?: context.getString(android.R.string.untitled)
    }

}