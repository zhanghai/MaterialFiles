/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.io.IOException;

import androidx.annotation.NonNull;
import java8.nio.file.Path;

public interface DirectoryObservableProvider {

    @NonNull
    DirectoryObservable observeDirectory(@NonNull Path directory, long intervalMillis)
            throws IOException;
}
