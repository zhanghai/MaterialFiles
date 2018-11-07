/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.storage.StorageVolume;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * @see StorageVolume
 * @see <a href="https://android.googlesource.com/platform/frameworks/base/+/ics-mr0-release/core/java/android/os/storage/StorageVolume.java">
 *      ics-mr0-release/StorageVolume.java</a>
 * @see <a href="https://android.googlesource.com/platform/prebuilts/runtime/+/master/appcompat/hiddenapi-light-greylist.txt">
 *      hiddenapi-light-greylist.txt</a>
 */
public class StorageVolumeCompat {

    @NonNull
    private static final Object sStorageVolumeGetPathMethodLock = new Object();
    @Nullable
    private static Method sStorageVolumeGetPathMethod;

    @NonNull
    private static final Object sStorageVolumeGetPathFileMethodLock = new Object();
    @Nullable
    private static Method sStorageVolumeGetPathFileMethod;

    @NonNull
    private static final Object sStorageVolumeGetDescriptionMethodLock = new Object();
    @Nullable
    private static Method sStorageVolumeGetDescriptionMethod;

    private StorageVolumeCompat() {}

    /**
     * &#064;see StorageVolume#getPath()
     */
    @NonNull
    @SuppressLint({"NewApi", "PrivateApi"})
    public static String getPath(@NonNull StorageVolume storageVolume) {
        //noinspection TryWithIdenticalCatches
        try {
            return (String) getStorageVolumeGetPathMethod().invoke(storageVolume);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    @SuppressLint({"NewApi", "PrivateApi"})
    private static Method getStorageVolumeGetPathMethod() {
        synchronized (sStorageVolumeGetPathMethodLock) {
            if (sStorageVolumeGetPathMethod == null) {
                try {
                    //noinspection JavaReflectionMemberAccess
                    sStorageVolumeGetPathMethod = StorageVolume.class.getDeclaredMethod("getPath");
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                sStorageVolumeGetPathMethod.setAccessible(true);
            }
            return sStorageVolumeGetPathMethod;
        }
    }

    /**
     * &#064;see StorageVolume#getPathFile()
     */
    @NonNull
    @SuppressLint({"NewApi", "PrivateApi"})
    public static File getPathFile(@NonNull StorageVolume storageVolume) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //noinspection TryWithIdenticalCatches
            try {
                return (File) getStorageVolumeGetPathFileMethod().invoke(storageVolume);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            String path = getPath(storageVolume);
            return new File(path);
        }
    }

    @NonNull
    @SuppressLint({"NewApi", "PrivateApi"})
    private static Method getStorageVolumeGetPathFileMethod() {
        synchronized (sStorageVolumeGetPathFileMethodLock) {
            if (sStorageVolumeGetPathFileMethod == null) {
                try {
                    //noinspection JavaReflectionMemberAccess
                    sStorageVolumeGetPathFileMethod = StorageVolume.class.getDeclaredMethod(
                            "getPathFile");
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                sStorageVolumeGetPathFileMethod.setAccessible(true);
            }
            return sStorageVolumeGetPathFileMethod;
        }
    }

    /**
     * @see StorageVolume#getDescription(Context)
     */
    @NonNull
    @SuppressLint("NewApi")
    public static String getDescription(@NonNull StorageVolume storageVolume,
                                        @NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return storageVolume.getDescription(context);
        } else {
            //noinspection TryWithIdenticalCatches
            try {
                return (String) getStorageVolumeGetDescriptionMethod().invoke(storageVolume);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @NonNull
    @SuppressLint("NewApi")
    private static Method getStorageVolumeGetDescriptionMethod() {
        synchronized (sStorageVolumeGetDescriptionMethodLock) {
            if (sStorageVolumeGetDescriptionMethod == null) {
                try {
                    //noinspection JavaReflectionMemberAccess
                    sStorageVolumeGetDescriptionMethod = StorageVolume.class.getDeclaredMethod(
                            "getDescription");
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                sStorageVolumeGetDescriptionMethod.setAccessible(true);
            }
            return sStorageVolumeGetDescriptionMethod;
        }
    }

    /**
     * @see StorageVolume#isPrimary()
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("NewApi")
    public static boolean isPrimary(@NonNull StorageVolume storageVolume) {
        return storageVolume.isPrimary();
    }

    /**
     * @see StorageVolume#isRemovable()
     */
    @SuppressLint("NewApi")
    public static boolean isRemovable(@NonNull StorageVolume storageVolume) {
        return storageVolume.isRemovable();
    }

    /**
     * @see StorageVolume#isEmulated()
     */
    @SuppressLint("NewApi")
    public static boolean isEmulated(@NonNull StorageVolume storageVolume) {
        return storageVolume.isEmulated();
    }

    /**
     * @see StorageVolume#getUuid()
     */
    @Nullable
    @RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)
    @SuppressLint("NewApi")
    public static String getUuid(@NonNull StorageVolume storageVolume) {
        return storageVolume.getUuid();
    }

    /**
     * @see StorageVolume#getState()
     */
    @NonNull
    @RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)
    @SuppressLint("NewApi")
    public static String getState(@NonNull StorageVolume storageVolume) {
        return storageVolume.getState();
    }
}
