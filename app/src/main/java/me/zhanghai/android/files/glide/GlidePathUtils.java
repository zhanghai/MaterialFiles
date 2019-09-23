/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import me.zhanghai.android.files.provider.document.DocumentFileSystemProvider;
import me.zhanghai.android.files.provider.document.resolver.DocumentResolver;
import me.zhanghai.android.files.settings.Settings;

public class GlidePathUtils {

    private GlidePathUtils() {}

    public static boolean shouldLoadThumbnail(@NonNull Path path) {
        if (DocumentFileSystemProvider.isDocumentPath(path)) {
            if (!(DocumentResolver.isLocal((DocumentResolver.Path) path)
                    || Settings.READ_REMOTE_FILES_FOR_THUMBNAIL.getValue())) {
                return false;
            }
        }
        return true;
    }
}
