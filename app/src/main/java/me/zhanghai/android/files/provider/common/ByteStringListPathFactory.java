/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import androidx.annotation.NonNull;
import java8.nio.file.FileSystem;
import java8.nio.file.Path;

public interface ByteStringListPathFactory {

    @NonNull
    ByteStringListPath getPath(@NonNull ByteString first, @NonNull ByteString... more);

    @NonNull
    static ByteStringListPath getPath(@NonNull FileSystem fileSystem, @NonNull ByteString first,
                                      @NonNull ByteString... more) {
        return ((ByteStringListPathFactory) fileSystem).getPath(first, more);
    }

    @NonNull
    static ByteStringListPath getPath(@NonNull Path path, @NonNull ByteString first,
                                      @NonNull ByteString... more) {
        return getPath(path.getFileSystem(), first, more);
    }
}
