/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.zhanghai.android.files.util.layoutInflater

/**
 * A modern [ListAdapter] implementation for static layouts or simple lists.
 * Refactored to use DiffUtil and clean Kotlin 1.9.0+ syntax.
 */
class StaticAdapter(
    @LayoutRes private val layoutRes: Int,
    private val listener: ((Int) -> Unit)? = null
) : ListAdapter<Any, StaticAdapter.ViewHolder>(StaticDiffCallback) {

    init {
        // ListAdapter uses DiffUtil for animations, but we can still enable stable IDs if needed
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent.context.layoutInflater.inflate(layoutRes, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Resolved TODO: Using safe call with invoke() for the nullable listener
        holder.itemView.setOnClickListener {
            listener?.invoke(position)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /**
     * Minimal DiffUtil callback. Since this is a "Static" adapter, 
     * items are often identical or simple markers.
     */
    private object StaticDiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean =
            oldItem == newItem
    }
}
