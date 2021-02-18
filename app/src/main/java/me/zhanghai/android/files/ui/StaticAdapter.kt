/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import me.zhanghai.android.files.util.layoutInflater

class StaticAdapter(
    @LayoutRes val layoutRes: Int,
    val listener: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<StaticAdapter.ViewHolder>() {
    init {
        setHasStableIds(true)
    }

    @get:JvmName("_getItemCount")
    var itemCount: Int = 1
        set(value) {
            if (field == value) {
                return
            }
            val oldValue = field
            field = value
            if (value < oldValue) {
                notifyItemRangeRemoved(value, oldValue - value)
            } else {
                notifyItemRangeInserted(oldValue, value - oldValue)
            }
        }

    override fun getItemCount(): Int = itemCount

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent.context.layoutInflater.inflate(layoutRes, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (listener != null) {
            // TODO: kotlinc: Reference has a nullable type '((Int) -> Unit)?', use explicit
            //  '?.invoke()' to make a function-like call instead
            //holder.itemView.setOnClickListener { listener(position) }
            holder.itemView.setOnClickListener { listener.invoke(position) }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
