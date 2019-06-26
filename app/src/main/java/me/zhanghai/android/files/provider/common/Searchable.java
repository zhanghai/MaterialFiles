/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import java9.util.function.Consumer;

public interface Searchable {

    void search(@NonNull Path directory, @NonNull String query,
                @NonNull Consumer<List<Path>> listener, long intervalMillis) throws IOException;
}
