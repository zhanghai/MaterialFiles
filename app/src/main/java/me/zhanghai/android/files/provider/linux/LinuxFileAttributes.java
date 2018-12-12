/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import java8.nio.file.attribute.FileAttribute;

class LinuxFileAttributes {

    private LinuxFileAttributes() {}

    @NonNull
    public static Set<LinuxFileModeBit> toMode(@NonNull FileAttribute<?>[] attributes,
                                               @NonNull Set<LinuxFileModeBit> defaultMode) {
        Set<LinuxFileModeBit> mode = null;
        for (FileAttribute<?> attribute : attributes) {
            Objects.requireNonNull(attribute);
            if (!Objects.equals(attribute.name(), LinuxFileModeAttribute.NAME)) {
                throw new UnsupportedOperationException(attribute.name());
            }
            Object value = attribute.value();
            Objects.requireNonNull(value);
            if (!(value instanceof Set)) {
                throw new UnsupportedOperationException(value.toString());
            }
            //noinspection unchecked
            mode = (Set<LinuxFileModeBit>) value;
            for (Object modeBit : mode) {
                Objects.requireNonNull(modeBit);
                if (!(modeBit instanceof LinuxFileModeBit)) {
                    throw new UnsupportedOperationException(modeBit.toString());
                }
            }
        }
        return mode != null ? mode : defaultMode;
    }
}
