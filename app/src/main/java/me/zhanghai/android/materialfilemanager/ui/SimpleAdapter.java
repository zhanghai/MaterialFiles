/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.ui;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class SimpleAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private List<T> mList = new ArrayList<>();

    public SimpleAdapter() {
        this(null);
    }

    public SimpleAdapter(List<T> list) {
        if (list != null) {
            mList.addAll(list);
        }
    }

    public List<T> getList() {
        return mList;
    }

    public void addAll(Collection<? extends T> collection) {
        int oldSize = mList.size();
        mList.addAll(collection);
        notifyItemRangeInserted(oldSize, collection.size());
    }

    public void replace(Collection<? extends T> collection) {
        mList.clear();
        mList.addAll(collection);
        notifyDataSetChanged();
    }

    public void add(int position, T item) {
        mList.add(position, item);
        notifyItemInserted(position);
    }

    public void add(T item) {
        add(mList.size(), item);
    }

    public void set(int position, T item) {
        mList.set(position, item);
        notifyItemChanged(position);
    }

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

    public T removeById(long id) {
        int position = findPositionById(id);
        if (position != RecyclerView.NO_POSITION) {
            return remove(position);
        } else {
            return null;
        }
    }

    public T getItem(int position) {
        return mList.get(position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
