/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import java.util.Set;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.ByteStringBuilder;
import me.zhanghai.android.files.provider.common.ByteStringListPathFactory;
import me.zhanghai.android.files.provider.common.MoreFiles;

public class PathFileNameUtils {

    private static final byte EXTENSION_SEPARATOR = '.';

    // https://github.com/linuxmint/nemo/blob/dfc39a5f13e0af38c088c091e7c2057b1c80e402/eel/eel-vfs-extensions.c#L130
    private static final Set<ByteString> TWO_SEPARATORS_EXTENSIONS =
            SetBuilder.<ByteString>newHashSet()
                    .add(ByteString.fromString("bz"))
                    .add(ByteString.fromString("bz2"))
                    .add(ByteString.fromString("gz"))
                    .add(ByteString.fromString("sit"))
                    .add(ByteString.fromString("Z"))
                    .add(ByteString.fromString("xz"))
                    .build();

    private PathFileNameUtils() {}

    @NonNull
    public static ByteString getFullBaseName(@NonNull ByteString fileName) {
        int index = indexOfFullExtensionSeparator(fileName);
        return index != -1 ? fileName.substring(0, index) : fileName;
    }

    @NonNull
    public static Path getFullBaseName(@NonNull Path fileName) {
        return ByteStringListPathFactory.getPath(fileName, getFullBaseName(MoreFiles.toByteString(
                fileName)));
    }

    @NonNull
    public static ByteString getFullExtension(@NonNull ByteString fileName) {
        int index = indexOfFullExtensionSeparator(fileName);
        return index != -1 ? fileName.substring(index + 1) : ByteString.EMPTY;
    }

    @NonNull
    public static Path getFullExtension(@NonNull Path fileName) {
        return ByteStringListPathFactory.getPath(fileName, getFullExtension(MoreFiles.toByteString(
                fileName)));
    }

    public static int indexOfFullExtensionSeparator(@NonNull ByteString fileName) {
        int lastExtensionSeparatorIndex = fileName.lastIndexOf(EXTENSION_SEPARATOR);
        if (lastExtensionSeparatorIndex == -1 || lastExtensionSeparatorIndex == 0) {
            return lastExtensionSeparatorIndex;
        }
        ByteString extension = fileName.substring(lastExtensionSeparatorIndex + 1);
        if (!TWO_SEPARATORS_EXTENSIONS.contains(extension)) {
            return lastExtensionSeparatorIndex;
        }
        int secondLastExtensionSeparatorIndex = fileName.lastIndexOf(EXTENSION_SEPARATOR,
                lastExtensionSeparatorIndex - 1);
        if (secondLastExtensionSeparatorIndex == -1) {
            return lastExtensionSeparatorIndex;
        }
        return secondLastExtensionSeparatorIndex;
    }

    public static int indexOfFullExtensionSeparator(@NonNull Path fileName) {
        return indexOfFullExtensionSeparator(MoreFiles.toByteString(fileName));
    }

    @NonNull
    public static ByteString replaceFullExtension(@NonNull ByteString fileName,
                                                  @NonNull ByteString extension) {
        ByteString baseName = getFullBaseName(fileName);
        if (extension.isEmpty()) {
            return baseName;
        }
        return new ByteStringBuilder(baseName)
                .append(EXTENSION_SEPARATOR)
                .append(extension)
                .toByteString();
    }

    @NonNull
    public static Path replaceFullExtension(@NonNull Path fileName, @NonNull ByteString extension) {
        return ByteStringListPathFactory.getPath(fileName, replaceFullExtension(
                MoreFiles.toByteString(fileName), extension));
    }
}
