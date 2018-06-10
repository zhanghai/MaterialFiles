/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.ui;

import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SortedListAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private List<T> mList = Collections.emptyList();
    private SortedList<T> mSortedList;

    protected void init(Class<T> classOfT, SortedList.Callback<T> callback) {
        mSortedList = new SortedList<>(classOfT, callback);
    }

    public void rebuild() {
        mSortedList.beginBatchedUpdates();
        for (int i = mSortedList.size() - 1; i >= 0; --i) {
            mSortedList.removeItemAt(i);
        }
        mSortedList.addAll(mList);
        mSortedList.endBatchedUpdates();
    }

    public void replaceAll(List<T> list) {
        mList = new ArrayList<>(list);
        mSortedList.replaceAll(mList);
    }

    protected T getItem(int position) {
        return mSortedList.get(position);
    }

    @Override
    public int getItemCount() {
        return mSortedList.size();
    }
}
