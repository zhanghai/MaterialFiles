/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import androidx.annotation.NonNull;

public class SearchViewState {

    public final boolean expanded;
    @NonNull
    public final String query;

    public SearchViewState(boolean expanded, @NonNull String query) {
        this.expanded = expanded;
        this.query = query;
    }
}
