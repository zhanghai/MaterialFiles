/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import java.util.Collections;
import java.util.Set;

import androidx.annotation.NonNull;
import java8.nio.file.attribute.FileAttribute;

class LinuxFileModeAttribute implements FileAttribute<Set<LinuxFileModeBit>> {

    public static final String NAME = "linux:mode";

    @NonNull
    private final Set<LinuxFileModeBit> mMode;

    public LinuxFileModeAttribute(@NonNull Set<LinuxFileModeBit> mode) {
        mMode = Collections.unmodifiableSet(mode);
    }

    @NonNull
    @Override
    public String name() {
        return NAME;
    }

    @NonNull
    @Override
    public Set<LinuxFileModeBit> value() {
        return mMode;
    }
}
