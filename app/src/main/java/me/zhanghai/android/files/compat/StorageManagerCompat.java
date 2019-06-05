/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import android.os.Build;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.ProxyFileDescriptorCallback;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import me.zhanghai.java.promise.Promise;
import me.zhanghai.java.reflected.ReflectedMethod;

/**
 * @see StorageManager
 * @see <a href="https://android.googlesource.com/platform/frameworks/base/+/ics-mr0-release/core/java/android/os/storage/StorageManager.java">
 *      ics-mr0-release/StorageManager.java</a>
 */
public class StorageManagerCompat {

    @NonNull
    private static final ReflectedMethod<StorageManager> sGetVolumeListMethod =
            new ReflectedMethod<>(StorageManager.class, "getVolumeList");

    private StorageManagerCompat() {}

    /**
     * @see StorageManager#getStorageVolumes()
     */
    @NonNull
    public static List<StorageVolume> getStorageVolumes(@NonNull StorageManager storageManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return storageManager.getStorageVolumes();
        } else {
            StorageVolume[] storageVolumes = sGetVolumeListMethod.invoke(storageManager);
            return Arrays.asList(storageVolumes);
        }
    }

    // Thanks to fython for https://gist.github.com/fython/924f8d9019bca75d22de116bb69a54a1
    /**
     * @see StorageManager#openProxyFileDescriptor(int, ProxyFileDescriptorCallback, Handler)
     */
    @NonNull
    public static ParcelFileDescriptor openProxyFileDescriptor(
            @NonNull StorageManager storageManager, int mode,
            @NonNull ProxyFileDescriptorCallbackCompat callback, @NonNull Handler handler)
            throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return storageManager.openProxyFileDescriptor(mode,
                    callback.toProxyFileDescriptorCallback(), handler);
        } else {
            // TODO: Support other modes?
            if (mode != ParcelFileDescriptor.MODE_READ_ONLY) {
                throw new UnsupportedOperationException("mode " + mode);
            }
            ParcelFileDescriptor[] pfds = ParcelFileDescriptor.createReliablePipe();
            new PipeWriter(pfds[1], callback, handler).start();
            return pfds[0];
        }
    }

    private static class PipeWriter extends Thread {

        private static final AtomicInteger sId = new AtomicInteger();

        @NonNull
        private final ParcelFileDescriptor mPfd;
        @NonNull
        private final ProxyFileDescriptorCallbackCompat mCallback;
        @NonNull
        private final Handler mHandler;

        PipeWriter(@NonNull ParcelFileDescriptor pfd,
                   @NonNull ProxyFileDescriptorCallbackCompat callback, @NonNull Handler handler) {
            super("StorageManagerCompat.PipeWriter-" + sId.getAndIncrement());

            mPfd = pfd;
            mCallback = callback;
            mHandler = handler;
        }

        @Override
        public void run() {
            try (ParcelFileDescriptor.AutoCloseOutputStream outputStream =
                         new ParcelFileDescriptor.AutoCloseOutputStream(mPfd)) {
                long[] offset = { 0 };
                byte[] buffer = new byte[4 * 1024];
                while (true) {
                    int size = new Promise<Integer>(settler -> mHandler.post(() -> {
                        try {
                            int size_ = mCallback.onRead(offset[0], buffer.length, buffer);
                            settler.resolve(size_);
                        } catch (Exception e) {
                            settler.reject(e);
                        }
                    })).await();
                    if (size == 0) {
                        break;
                    }
                    offset[0] += size;
                    outputStream.write(buffer, 0, size);
                }
                new Promise<Void>(settler -> mHandler.post(() -> {
                    try {
                        mCallback.onRelease();
                        settler.resolve(null);
                    } catch (Exception e) {
                        settler.reject(e);
                    }
                })).await();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    mPfd.closeWithError(e.getMessage());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
