/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.text;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import me.zhanghai.android.files.util.StatefulData;

public class FileContentData extends StatefulData<byte[]> {

    @NonNull
    public final Path path;

    private FileContentData(@NonNull State state, @NonNull Path path, @Nullable byte[] data,
                            @Nullable Exception exception) {
        super(state, data, exception);

        this.path = path;
    }

    @NonNull
    public static FileContentData ofLoading(@NonNull Path path) {
        return new FileContentData(State.LOADING, path, null, null);
    }

    @NonNull
    public static FileContentData ofError(@NonNull Path path, @NonNull Exception exception) {
        return new FileContentData(State.ERROR, path, null, exception);
    }

    @NonNull
    public static FileContentData ofSuccess(@NonNull Path path, @NonNull byte[] content) {
        return new FileContentData(State.SUCCESS, path, content, null);
    }
}
