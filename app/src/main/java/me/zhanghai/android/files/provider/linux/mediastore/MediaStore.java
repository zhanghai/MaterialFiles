/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.mediastore;

import android.media.MediaScannerConnection;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import java8.nio.channels.FileChannel;
import me.zhanghai.android.files.AppProvider;
import me.zhanghai.android.files.provider.common.DelegateFileChannel;
import me.zhanghai.android.files.provider.root.RootUtils;

/*
 * @see com.android.internal.content.FileSystemProvider
 * @see com.android.providers.media.scan.ModernMediaScanner.java
 */
public class MediaStore {

    private MediaStore() {}

    public static void scan(@NonNull File file) {
        if (RootUtils.isRunningAsRoot()) {
            return;
        }
        MediaScannerConnection.scanFile(AppProvider.requireContext(),
                new String[] { file.getPath() }, null, null);
    }

    @NonNull
    public static FileChannel newScanOnCloseFileChannel(@NonNull FileChannel fileChannel,
                                                        @NonNull File file) {
        if (RootUtils.isRunningAsRoot()) {
            return fileChannel;
        }
        return new DelegateFileChannel(fileChannel) {
            @Override
            protected void implCloseChannel() throws IOException {
                super.implCloseChannel();

                scan(file);
            }
        };
    }
}
