/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.content;

import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;
import java8.nio.channels.FileChannel;
import java8.nio.channels.FileChannels;
import me.zhanghai.android.files.compat.NioUtilsCompat;

class ContentFileChannels {

    private ContentFileChannels() {}

    @NonNull
    public static FileChannel open(@NonNull ParcelFileDescriptor pfd, @NonNull String mode) {
        return FileChannels.from(NioUtilsCompat.newFileChannel(pfd, pfd.getFileDescriptor(),
                ParcelFileDescriptor.parseMode(mode)));
    }
}
