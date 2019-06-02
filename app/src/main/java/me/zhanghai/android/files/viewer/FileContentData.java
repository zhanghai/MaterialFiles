/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;

public class FileContentData {

    public enum State {
        LOADING,
        ERROR,
        SUCCESS
    }

    @NonNull
    public final State state;
    @NonNull
    public final Path path;
    @Nullable
    public final byte[] content;
    @Nullable
    public final Exception exception;

    private FileContentData(@NonNull State state, @NonNull Path path, @Nullable byte[] content,
                            @Nullable Exception exception) {
        this.state = state;
        this.path = path;
        this.content = content;
        this.exception = exception;
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
