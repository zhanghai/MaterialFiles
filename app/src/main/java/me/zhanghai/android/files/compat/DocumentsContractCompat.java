/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;

public class DocumentsContractCompat {

    private DocumentsContractCompat() {}

    public static boolean isTreeUri(@NonNull Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return DocumentsContract.isTreeUri(uri);
        } else {
            List<String> paths = uri.getPathSegments();
            return paths.size() >= 2 && Objects.equals(paths.get(0), "tree");
        }
    }
}
