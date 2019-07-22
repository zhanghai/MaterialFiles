/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import java8.nio.file.FileSystem;
import java8.nio.file.FileSystemAlreadyExistsException;
import java8.nio.file.FileSystemNotFoundException;
import java9.util.function.Supplier;

public class FileSystemCache<K, FS extends FileSystem> {

    @NonNull
    private final Map<K, WeakReference<FS>> mFileSystems = new HashMap<>();
    @NonNull
    private final Object mLock = new Object();

    @NonNull
    public FS getOrNew(@NonNull K key, Supplier<FS> newFileSystem) {
        FS fileSystem;
        synchronized (mLock) {
            WeakReference<FS> fileSystemReference = mFileSystems.get(key);
            if (fileSystemReference != null) {
                fileSystem = fileSystemReference.get();
                if (fileSystem != null) {
                    return fileSystem;
                }
            }
            fileSystem = newFileSystem.get();
            mFileSystems.put(key, new WeakReference<>(fileSystem));
        }
        return fileSystem;
    }

    @NonNull
    public FS new_(@NonNull K key, Supplier<FS> newFileSystem) {
        FS fileSystem;
        synchronized (mLock) {
            WeakReference<FS> fileSystemReference = mFileSystems.get(key);
            if (fileSystemReference != null) {
                fileSystem = fileSystemReference.get();
                if (fileSystem != null) {
                    throw new FileSystemAlreadyExistsException(key.toString());
                }
            }
            fileSystem = newFileSystem.get();
            mFileSystems.put(key, new WeakReference<>(fileSystem));
        }
        return fileSystem;
    }

    @NonNull
    public FS get(@NonNull K key) {
        FS fileSystem = null;
        synchronized (mLock) {
            WeakReference<FS> fileSystemReference = mFileSystems.get(key);
            if (fileSystemReference != null) {
                fileSystem = fileSystemReference.get();
                if (fileSystem == null) {
                    mFileSystems.remove(key);
                }
            }
        }
        if (fileSystem == null) {
            throw new FileSystemNotFoundException(key.toString());
        }
        return fileSystem;
    }

    public void remove(@NonNull K key, @NonNull FS fileSystem) {
        synchronized (mLock) {
            WeakReference<FS> fileSystemReference = mFileSystems.get(key);
            if (fileSystemReference == null) {
                return;
            }
            FS currentFileSystem = fileSystemReference.get();
            if (currentFileSystem == null || currentFileSystem == fileSystem) {
                mFileSystems.remove(key);
            }
        }
    }
}
