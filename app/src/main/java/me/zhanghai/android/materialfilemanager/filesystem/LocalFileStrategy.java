/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.os.Parcelable;
import android.support.annotation.WorkerThread;

import org.threeten.bp.Instant;

import java.util.List;
import java.util.Set;

interface LocalFileStrategy extends Parcelable {

    boolean hasInformation();

    @WorkerThread
    void reloadInformation(LocalFile file) throws FileSystemException;

    boolean isDirectory();

    boolean isSymbolicLink();

    Set<PosixFileModeBit> getMode();

    PosixUser getOwner();

    PosixGroup getGroup();

    long getSize();

    Instant getLastModificationTime();

    @WorkerThread
    List<File> getChildren(LocalFile file) throws FileSystemException;


    @Override
    boolean equals(Object object);

    @Override
    int hashCode();
}
