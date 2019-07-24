/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filesystem;

import android.net.Uri;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import me.zhanghai.android.files.AppApplication;

public class PersistedDocumentTreeUrisLiveData extends LiveData<List<Uri>> {

    @Nullable
    private static PersistedDocumentTreeUrisLiveData sInstance;

    @NonNull
    public static PersistedDocumentTreeUrisLiveData getInstance() {
        if (sInstance == null) {
            sInstance = new PersistedDocumentTreeUrisLiveData();
        }
        return sInstance;
    }

    private PersistedDocumentTreeUrisLiveData() {
        // Initialize value before we have any active observer.
        loadValue();
    }

    public void loadValue() {
        List<Uri> treeUris = DocumentTree.getPersistedUris(AppApplication.getInstance());
        setValue(treeUris);
    }
}
