/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import me.zhanghai.java.reflected.ReflectedMethod;

/**
 * @see StorageVolume
 * @see <a href="https://android.googlesource.com/platform/frameworks/base/+/ics-mr0-release/core/java/android/os/storage/StorageVolume.java">
 *      ics-mr0-release/StorageVolume.java</a>
 * @see <a href="https://android.googlesource.com/platform/prebuilts/runtime/+/master/appcompat/hiddenapi-light-greylist.txt">
 *      hiddenapi-light-greylist.txt</a>
 */
public class StorageVolumeCompat {

    @NonNull
    @SuppressLint("NewApi")
    private static final ReflectedMethod<StorageVolume> sGetPathMethod = new ReflectedMethod<>(
            StorageVolume.class, "getPath");

    @NonNull
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("NewApi")
    private static final ReflectedMethod<StorageVolume> sGetPathFileMethod = new ReflectedMethod<>(
            StorageVolume.class, "getPathFile");

    @NonNull
    @SuppressLint("NewApi")
    private static final ReflectedMethod<StorageVolume> sGetDescriptionMethod =
            new ReflectedMethod<>(StorageVolume.class, "getDescription");

    private StorageVolumeCompat() {}

    /*
     * @see StorageVolume#getPath()
     */
    @NonNull
    public static String getPath(@NonNull StorageVolume storageVolume) {
        return sGetPathMethod.invoke(storageVolume);
    }

    /*
     * @see StorageVolume#getPathFile()
     */
    @NonNull
    public static File getPathFile(@NonNull StorageVolume storageVolume) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return sGetPathFileMethod.invoke(storageVolume);
        } else {
            String path = getPath(storageVolume);
            return new File(path);
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
            return sGetDescriptionMethod.invoke(storageVolume);
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

    @NonNull
    public static Intent createOpenDocumentTreeIntent(@NonNull StorageVolume storageVolume) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return storageVolume.createOpenDocumentTreeIntent();
        } else {
            String rootId = isEmulated(storageVolume) ?
                    DocumentsContractCompat.EXTERNAL_STORAGE_PRIMARY_EMULATED_ROOT_ID
                    : getUuid(storageVolume);
            Uri rootUri = DocumentsContract.buildRootUri(
                    DocumentsContractCompat.EXTERNAL_STORAGE_PROVIDER_AUTHORITY, rootId);
            return new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                    .putExtra(DocumentsContract.EXTRA_INITIAL_URI, rootUri)
                    .putExtra(DocumentsContractCompat.EXTRA_SHOW_ADVANCED, true);
        }
    }
}
