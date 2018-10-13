/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class Documents {

    public static Intent makeOpenTreeIntent() {
        return new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
    }

    public static void takePersistableTreePermission(Uri treeUri, Context context) {
        context.getContentResolver().takePersistableUriPermission(treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }
}
