package com.kroegerama.kaiteki.recyclerview

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration

fun RecyclerView.addDividers(isLastItemDecorated: Boolean = true) = addItemDecoration(
    MaterialDividerItemDecoration(
        context,
        MaterialDividerItemDecoration.VERTICAL
    ).also {
        it.isLastItemDecorated = isLastItemDecorated
    }
)
