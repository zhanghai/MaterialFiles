/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public abstract class ListAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    @NonNull
    private final ListDiffer<T> mListDiffer;

    public ListAdapter(@NonNull DiffUtil.ItemCallback<T> callback) {
        mListDiffer = new ListDiffer<T>(new AdapterListUpdateCallback(this), callback);
        setHasStableIds(getHasStableIds());
    }

    protected abstract boolean getHasStableIds();

    @NonNull
    public List<T> getList() {
        return mListDiffer.getList();
    }

    public void refresh() {
        List<T> list = mListDiffer.getList();
        mListDiffer.setList(Collections.emptyList());
        mListDiffer.setList(list);
    }

    public void replace(@NonNull List<T> list, boolean clear) {
        if (clear) {
            mListDiffer.setList(Collections.emptyList());
        }
        mListDiffer.setList(list);
    }

    public void clear() {
        mListDiffer.setList(Collections.emptyList());
    }

    public T getItem(int position) {
        return getList().get(position);
    }

    @Override
    public int getItemCount() {
        return getList().size();
    }
}
