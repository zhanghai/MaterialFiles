/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.CopyOption;
import java8.nio.file.LinkOption;
import java8.nio.file.StandardCopyOption;
import java9.util.function.LongConsumer;

public class CopyOptions {

    private final boolean mReplaceExisting;
    private final boolean mCopyAttributes;
    private final boolean mAtomicMove;
    private final boolean mNoFollowLinks;
    @Nullable
    private final LongConsumer mProgressListener;
    private final int mProgressIntervalMillis;

    public CopyOptions(boolean replaceExisting, boolean copyAttributes, boolean atomicMove,
                       boolean noFollowLinks, @Nullable LongConsumer progressListener,
                       int progressIntervalMillis) {
        mReplaceExisting = replaceExisting;
        mCopyAttributes = copyAttributes;
        mAtomicMove = atomicMove;
        mNoFollowLinks = noFollowLinks;
        mProgressListener = progressListener;
        mProgressIntervalMillis = progressIntervalMillis;
    }

    @NonNull
    public static CopyOptions fromArray(@NonNull CopyOption... options) {
        boolean replaceExisting = false;
        boolean copyAttributes = false;
        boolean atomicMove = false;
        boolean noFollowLinks = false;
        LongConsumer progressListener = null;
        int progressIntervalMillis = 0;
        for (CopyOption option : options) {
            Objects.requireNonNull(option);
            if (option instanceof StandardCopyOption) {
                StandardCopyOption standardCopyOption = (StandardCopyOption) option;
                switch (standardCopyOption) {
                    case REPLACE_EXISTING:
                        replaceExisting = true;
                        break;
                    case COPY_ATTRIBUTES:
                        copyAttributes = true;
                        break;
                    case ATOMIC_MOVE:
                        atomicMove = true;
                        break;
                    default:
                        throw new UnsupportedOperationException(standardCopyOption.toString());
                }
            } else if (option == LinkOption.NOFOLLOW_LINKS) {
                noFollowLinks = true;
            } else if (option instanceof ProgressCopyOption) {
                ProgressCopyOption progressCopyOption = (ProgressCopyOption) option;
                progressListener = progressCopyOption.getListener();
                progressIntervalMillis = progressCopyOption.getIntervalMillis();
            } else {
                throw new UnsupportedOperationException(option.toString());
            }
        }
        return new CopyOptions(replaceExisting, copyAttributes, atomicMove, noFollowLinks,
                progressListener, progressIntervalMillis);
    }

    public boolean hasReplaceExisting() {
        return mReplaceExisting;
    }

    public boolean hasCopyAttributes() {
        return mCopyAttributes;
    }

    public boolean hasAtomicMove() {
        return mAtomicMove;
    }

    public boolean hasNoFollowLinks() {
        return mNoFollowLinks;
    }

    public boolean hasProgressListener() {
        return mProgressListener != null;
    }

    @Nullable
    public LongConsumer getProgressListener() {
        return mProgressListener;
    }

    public int getProgressIntervalMillis() {
        return mProgressIntervalMillis;
    }
}
