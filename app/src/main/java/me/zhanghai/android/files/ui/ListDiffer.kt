/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback

class ListDiffer<T>(
    private val updateCallback: ListUpdateCallback,
    private val diffCallback: DiffUtil.ItemCallback<T>
) {
    private var _list: List<T> = emptyList()
    var list: List<T>
        get() = _list
        set(newList) {
            if (newList === _list || newList.isEmpty() && _list.isEmpty()) {
                return
            }
            if (newList.isEmpty()) {
                val oldListSize = _list.size
                _list = emptyList()
                updateCallback.onRemoved(0, oldListSize)
                return
            }
            if (_list.isEmpty()) {
                _list = newList
                updateCallback.onInserted(0, newList.size)
                return
            }
            val oldList = _list
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return oldList.size
                }

                override fun getNewListSize(): Int {
                    return newList.size
                }

                override fun areItemsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    val oldItem: T? = oldList[oldItemPosition]
                    val newItem: T? = newList[newItemPosition]
                    return if (oldItem != null && newItem != null) {
                        diffCallback.areItemsTheSame(oldItem, newItem)
                    } else {
                        oldItem == null && newItem == null
                    }
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    val oldItem: T? = oldList[oldItemPosition]
                    val newItem: T? = newList[newItemPosition]
                    return if (oldItem != null && newItem != null) {
                        diffCallback.areContentsTheSame(oldItem, newItem)
                    } else if (oldItem == null && newItem == null) {
                        true
                    } else {
                        throw AssertionError()
                    }
                }

                override fun getChangePayload(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Any? {
                    val oldItem: T? = oldList[oldItemPosition]
                    val newItem: T? = newList[newItemPosition]
                    return if (oldItem != null && newItem != null) {
                        diffCallback.getChangePayload(oldItem, newItem)
                    } else {
                        throw AssertionError()
                    }
                }
            })
            _list = newList
            result.dispatchUpdatesTo(updateCallback)
        }
}
