/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter

class UnfilteredArrayAdapter<T> : ArrayAdapter<T> {
    private val filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults = FilterResults()

        override fun publishResults(constraint: CharSequence, results: FilterResults) {}
    }

    constructor(
        context: Context,
        resource: Int,
        textViewResourceId: Int = 0,
        objects: List<T> = emptyList()
    ) : super(context, resource, textViewResourceId, objects)

    constructor(
        context: Context,
        resource: Int,
        textViewResourceId: Int = 0,
        objects: Array<out T>
    ) : super(context, resource, textViewResourceId, objects)

    override fun getFilter(): Filter = filter
}
