/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import androidx.annotation.NonNull;

public class SearchState {

    public final boolean searching;
    @NonNull
    public final String query;

    public SearchState(boolean searching, @NonNull String query) {
        this.searching = searching;
        this.query = query;
    }
}
