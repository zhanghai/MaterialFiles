/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filesystem;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

public class Documents {

    @NonNull
    public static Intent makeOpenTreeIntent() {
        // TODO: Consider StorageVolume.createAccessIntent().
        return new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
    }

    public static void takePersistableUriPermission(@NonNull Uri uri, @NonNull Context context) {
        if (takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION, context)) {
            return;
        }
        // The provider should have provided us with both permissions, but at least let's try if
        // we can still take the read permission.
        takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION, context);
    }

    private static boolean takePersistableUriPermission(@NonNull Uri uri, int modeFlags,
                                                        @NonNull Context context) {
        try {
            context.getContentResolver().takePersistableUriPermission(uri, modeFlags);
            return true;
        } catch (SecurityException e) {
            e.printStackTrace();
            return false;
        }
    }
}
