/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public abstract class SimpleAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    @NonNull
    private final List<T> mList = new ArrayList<>();

    public SimpleAdapter() {
        setHasStableIds(getHasStableIds());
    }

    protected abstract boolean getHasStableIds();

    @NonNull
    public List<T> getList() {
        return mList;
    }

    public void addAll(@NonNull Collection<? extends T> collection) {
        int oldSize = mList.size();
        mList.addAll(collection);
        notifyItemRangeInserted(oldSize, collection.size());
    }

    public void replace(@NonNull Collection<? extends T> collection) {
        mList.clear();
        mList.addAll(collection);
        notifyDataSetChanged();
    }

    public void add(int position, @Nullable T item) {
        mList.add(position, item);
        notifyItemInserted(position);
    }

    public void add(@Nullable T item) {
        add(mList.size(), item);
    }

    public void set(int position, @Nullable T item) {
        mList.set(position, item);
        notifyItemChanged(position);
    }

    @Nullable
    public T remove(int position) {
        T item = mList.remove(position);
        notifyItemRemoved(position);
        return item;
    }

    public void clear() {
        int oldSize = mList.size();
        mList.clear();
        notifyItemRangeRemoved(0, oldSize);
    }

    public int findPositionById(long id) {
        int count = getItemCount();
        for (int i = 0; i < count; ++i) {
            if (getItemId(i) == id) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    public void notifyItemChangedById(long id) {
        int position = findPositionById(id);
        if (position != RecyclerView.NO_POSITION) {
            notifyItemChanged(position);
        }
    }

    @Nullable
    public T removeById(long id) {
        int position = findPositionById(id);
        if (position != RecyclerView.NO_POSITION) {
            return remove(position);
        } else {
            return null;
        }
    }

    @Nullable
    public T getItem(int position) {
        return mList.get(position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
