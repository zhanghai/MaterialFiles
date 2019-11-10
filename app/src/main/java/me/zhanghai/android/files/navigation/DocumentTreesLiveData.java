/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation;

import android.net.Uri;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import me.zhanghai.android.files.AppProvider;
import me.zhanghai.android.files.navigation.file.DocumentTree;

public class DocumentTreesLiveData extends LiveData<List<Uri>> {

    @Nullable
    private static DocumentTreesLiveData sInstance;

    @NonNull
    public static DocumentTreesLiveData getInstance() {
        if (sInstance == null) {
            sInstance = new DocumentTreesLiveData();
        }
        return sInstance;
    }

    private DocumentTreesLiveData() {
        // Initialize value before we have any active observer.
        loadValue();
    }

    public void loadValue() {
        List<Uri> treeUris = DocumentTree.getPersistedTreeUris(AppProvider.requireContext());
        setValue(treeUris);
    }
}
