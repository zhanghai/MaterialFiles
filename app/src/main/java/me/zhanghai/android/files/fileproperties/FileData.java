/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.file.FileItem;
import me.zhanghai.android.files.util.StatefulData;

public class FileData extends StatefulData<FileItem> {

    private FileData(@NonNull State state, @Nullable FileItem data,
                     @Nullable Exception exception) {
        super(state, data, exception);
    }

    @NonNull
    public static FileData ofLoading(@Nullable FileItem file) {
        return new FileData(State.LOADING, file, null);
    }

    @NonNull
    public static FileData ofLoading() {
        return ofLoading(null);
    }

    @NonNull
    public static FileData ofError(@NonNull Exception exception) {
        return new FileData(State.ERROR, null, exception);
    }

    @NonNull
    public static FileData ofSuccess(@NonNull FileItem file) {
        return new FileData(State.SUCCESS, file, null);
    }
}
