/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.os.Parcelable;
import android.support.annotation.WorkerThread;

import org.threeten.bp.Instant;

import java.util.List;

interface LocalFileStrategy extends Parcelable {

    @WorkerThread
    void loadInformation(LocalFile file) throws FileSystemException;

    long getSize();

    Instant getLastModificationTime();

    boolean isDirectory();

    boolean isSymbolicLink();

    @WorkerThread
    List<File> getChildren(LocalFile file) throws FileSystemException;


    @Override
    boolean equals(Object object);

    @Override
    int hashCode();
}
