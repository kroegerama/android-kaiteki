package com.kroegerama.kaiteki

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes

open class NoFilterArrayAdapter<T>(
    context: Context,
    @LayoutRes resource: Int = android.R.layout.simple_list_item_1,
    @IdRes textViewResourceId: Int = 0,
    objects: List<T> = ArrayList()
) : ArrayAdapter<T>(context, resource, textViewResourceId, objects) {

    private val noOpFilter = object : Filter() {
        private val noOpResult = FilterResults()
        override fun performFiltering(constraint: CharSequence?) = noOpResult
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {}
    }

    override fun getFilter() = noOpFilter
}
