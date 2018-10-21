/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.ui;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

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

    @Nullable
    protected T getItem(int position) {
        return mSortedList.get(position);
    }

    @Override
    public int getItemCount() {
        return mSortedList.size();
    }
}
