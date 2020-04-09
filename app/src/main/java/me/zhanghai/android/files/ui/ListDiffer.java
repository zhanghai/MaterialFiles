/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;

public class ListDiffer<T> {
    private final ListUpdateCallback mUpdateCallback;
    private final DiffUtil.ItemCallback<T> mDiffCallback;

    public ListDiffer(@NonNull ListUpdateCallback updateCallback,
                      @NonNull DiffUtil.ItemCallback<T> diffCallback) {
        mUpdateCallback = updateCallback;
        mDiffCallback = diffCallback;
    }

    @NonNull
    private List<T> mList = Collections.emptyList();
    @NonNull
    private List<T> mReadOnlyList = Collections.emptyList();

    @NonNull
    public List<T> getList() {
        return mReadOnlyList;
    }

    public void setList(@NonNull List<T> newList) {
        if (newList == mList || newList.isEmpty() && mList.isEmpty()) {
            return;
        }

        if (newList.isEmpty()) {
            int oldListSize = mList.size();
            mList = Collections.emptyList();
            mReadOnlyList = mList;
            mUpdateCallback.onRemoved(0, oldListSize);
            return;
        }

        if (mList.isEmpty()) {
            mList = newList;
            mReadOnlyList = Collections.unmodifiableList(newList);
            mUpdateCallback.onInserted(0, newList.size());
            return;
        }

        List<T> oldList = mList;
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                T oldItem = oldList.get(oldItemPosition);
                T newItem = newList.get(newItemPosition);
                if (oldItem != null && newItem != null) {
                    return mDiffCallback.areItemsTheSame(oldItem, newItem);
                } else {
                    return oldItem == null && newItem == null;
                }
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                T oldItem = oldList.get(oldItemPosition);
                T newItem = newList.get(newItemPosition);
                if (oldItem != null && newItem != null) {
                    return mDiffCallback.areContentsTheSame(oldItem, newItem);
                } else if (oldItem == null && newItem == null) {
                    return true;
                } else {
                    throw new AssertionError();
                }
            }

            @Nullable
            @Override
            public Object getChangePayload(int oldItemPosition, int newItemPosition) {
                T oldItem = oldList.get(oldItemPosition);
                T newItem = newList.get(newItemPosition);
                if (oldItem != null && newItem != null) {
                    return mDiffCallback.getChangePayload(oldItem, newItem);
                } else {
                    throw new AssertionError();
                }
            }
        });
        mList = newList;
        mReadOnlyList = Collections.unmodifiableList(mList);
        result.dispatchUpdatesTo(mUpdateCallback);
    }
}
