/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

public class SelectionLiveData<Key> extends MutableLiveData<Key> {

    public static final Object PAYLOAD_SELECTION_CHANGED = new Object();

    public void observe(@NonNull LifecycleOwner owner, @NonNull RecyclerView.Adapter<?> adapter) {
        observe(owner, key -> adapter.notifyItemRangeChanged(0, adapter.getItemCount(),
                PAYLOAD_SELECTION_CHANGED));
    }
}
