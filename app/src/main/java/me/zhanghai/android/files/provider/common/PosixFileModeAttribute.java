/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.util.Collections;
import java.util.Set;

import androidx.annotation.NonNull;
import java8.nio.file.attribute.FileAttribute;

class PosixFileModeAttribute implements FileAttribute<Set<PosixFileModeBit>> {

    public static final String NAME = "posix:mode";

    @NonNull
    private final Set<PosixFileModeBit> mMode;

    public PosixFileModeAttribute(@NonNull Set<PosixFileModeBit> mode) {
        mMode = Collections.unmodifiableSet(mode);
    }

    @NonNull
    @Override
    public String name() {
        return NAME;
    }

    @NonNull
    @Override
    public Set<PosixFileModeBit> value() {
        return mMode;
    }
}
