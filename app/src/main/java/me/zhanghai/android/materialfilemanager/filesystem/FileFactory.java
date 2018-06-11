/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;

public class FileFactory {

    private FileFactory() {}

    public static File create(Uri path) {
        switch (path.getScheme()) {
            case LocalFile.SCHEME:
                return new JavaLocalFile(path);
            case ArchiveFile.SCHEME:
                return new ArchiveFile(path);
            default:
                throw new UnsupportedOperationException("Unknown path: " + path);
        }
    }
}
