/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;

import androidx.annotation.NonNull;
import java8.nio.file.Path;

public interface DirectoryObservable extends Closeable {

    void addObserver(@NonNull Runnable observer);

    void removeObserver(@NonNull Runnable observer);

    @NonNull
    static DirectoryObservable observeDirectory(@NonNull Path path) throws IOException {
        Objects.requireNonNull(path);
        DirectoryObservableProvider provider = (DirectoryObservableProvider)
                path.getFileSystem().provider();
        return provider.observeDirectory(path);
    }
}
