/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

public abstract class SortedListAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    @NonNull
    private final List<T> mList = new ArrayList<>();
    @NonNull
    private SortedList<T> mSortedList;

    protected void init(@NonNull Class<T> classOfT, @NonNull SortedList.Callback<T> callback) {
        mSortedList = new SortedList<>(classOfT, callback);
    }

    public void refresh() {
        mSortedList.beginBatchedUpdates();
        mSortedList.clear();
        mSortedList.addAll(mList);
        mSortedList.endBatchedUpdates();
    }

    public void replace(@NonNull List<T> list) {
        mList.clear();
        mList.addAll(list);
        mSortedList.replaceAll(mList);
    }

    public void clear() {
        mList.clear();
        mSortedList.clear();
    }

    protected T getItem(int position) {
        return mSortedList.get(position);
    }

    @Override
    public int getItemCount() {
        return mSortedList.size();
    }
}
