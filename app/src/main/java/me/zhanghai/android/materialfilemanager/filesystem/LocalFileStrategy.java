/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.threeten.bp.Instant;

import java.util.List;
import java.util.Set;

interface LocalFileStrategy extends Parcelable {

    boolean hasInformation();

    @WorkerThread
    void reloadInformation(@NonNull LocalFile file) throws FileSystemException;

    boolean isSymbolicLink();

    boolean isSymbolicLinkBroken();

    @NonNull
    String getSymbolicLinkTarget();

    @NonNull
    PosixFileType getType();

    @NonNull
    Set<PosixFileModeBit> getMode();

    @NonNull
    PosixUser getOwner();

    @NonNull
    PosixGroup getGroup();

    long getSize();

    @NonNull
    Instant getLastModificationTime();

    @NonNull
    @WorkerThread
    List<File> getChildren(@NonNull LocalFile file) throws FileSystemException;


    @Override
    boolean equals(@Nullable Object object);

    @Override
    int hashCode();
}
