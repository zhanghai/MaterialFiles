/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.util;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * @see StorageManager
 * @see <a href="https://android.googlesource.com/platform/frameworks/base/+/ics-mr0-release/core/java/android/os/storage/StorageManager.java">
 *      ics-mr0-release/StorageManager.java</a>
 */
public class StorageManagerCompat {

    @NonNull
    private static final Object sStorageManagerGetVolumeListMethodLock = new Object();
    private static boolean sStorageManagerGetVolumeListMethodInitialized;
    @Nullable
    private static Method sStorageManagerGetVolumeListMethod;

    private StorageManagerCompat() {}

    /**
     * @see StorageManager#getStorageVolumes()
     */
    @NonNull
    public static List<StorageVolume> getStorageVolumes(@NonNull StorageManager storageManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return storageManager.getStorageVolumes();
        } else {
            StorageVolume[] storageVolumes;
            //noinspection TryWithIdenticalCatches
            try {
                storageVolumes = (StorageVolume[]) getStorageManagerGetVolumeListMethod().invoke(
                        storageManager);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            return Arrays.asList(storageVolumes);
        }
    }

    @NonNull
    @SuppressLint("PrivateApi")
    private static Method getStorageManagerGetVolumeListMethod() {
        synchronized (sStorageManagerGetVolumeListMethodLock) {
            if (!sStorageManagerGetVolumeListMethodInitialized) {
                try {
                    //noinspection JavaReflectionMemberAccess
                    sStorageManagerGetVolumeListMethod = StorageManager.class.getDeclaredMethod(
                            "getVolumeList");
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                sStorageManagerGetVolumeListMethod.setAccessible(true);
                sStorageManagerGetVolumeListMethodInitialized = true;
            }
            return sStorageManagerGetVolumeListMethod;
        }
    }
}
