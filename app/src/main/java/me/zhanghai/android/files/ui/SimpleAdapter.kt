/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import androidx.recyclerview.widget.RecyclerView

abstract class SimpleAdapter<T, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {
    private val _list = mutableListOf<T>()
    val list: List<T>
        get() = _list

    protected abstract val hasStableIds: Boolean

    init {
        setHasStableIds(hasStableIds)
    }

    fun addAll(collection: Collection<T>) {
        val oldSize = _list.size
        _list.addAll(collection)
        notifyItemRangeInserted(oldSize, collection.size)
    }

    fun replace(collection: Collection<T>) {
        _list.clear()
        _list.addAll(collection)
        notifyDataSetChanged()
    }

    fun add(position: Int, item: T) {
        _list.add(position, item)
        notifyItemInserted(position)
    }

    fun add(item: T) {
        add(_list.size, item)
    }

    operator fun set(position: Int, item: T) {
        _list[position] = item
        notifyItemChanged(position)
    }

    fun remove(position: Int): T {
        val item = _list.removeAt(position)
        notifyItemRemoved(position)
        return item
    }

    fun clear() {
        val oldSize = _list.size
        _list.clear()
        notifyItemRangeRemoved(0, oldSize)
    }

    fun findPositionById(id: Long): Int {
        val count = itemCount
        for (index in 0 until count) {
            if (getItemId(index) == id) {
                return index
            }
        }
        return RecyclerView.NO_POSITION
    }

    fun notifyItemChangedById(id: Long) {
        val position = findPositionById(id)
        if (position != RecyclerView.NO_POSITION) {
            notifyItemChanged(position)
        }
    }

    fun removeById(id: Long): T? {
        val position = findPositionById(id)
        return if (position != RecyclerView.NO_POSITION) {
            remove(position)
        } else {
            null
        }
    }

    fun getItem(position: Int): T {
        return _list[position]
    }

    override fun getItemCount(): Int {
        return _list.size
    }
}
