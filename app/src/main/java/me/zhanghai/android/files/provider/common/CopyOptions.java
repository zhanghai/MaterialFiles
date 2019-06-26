/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.util.ArrayList;
import java.util.List;
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
    private final long mProgressIntervalMillis;

    public CopyOptions(boolean replaceExisting, boolean copyAttributes, boolean atomicMove,
                       boolean noFollowLinks, @Nullable LongConsumer progressListener,
                       long progressIntervalMillis) {
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
        long progressIntervalMillis = 0;
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

    public long getProgressIntervalMillis() {
        return mProgressIntervalMillis;
    }

    @NonNull
    public CopyOption[] toArray() {
        List<CopyOption> options = new ArrayList<>();
        if (mReplaceExisting) {
            options.add(StandardCopyOption.REPLACE_EXISTING);
        }
        if (mCopyAttributes) {
            options.add(StandardCopyOption.COPY_ATTRIBUTES);
        }
        if (mAtomicMove) {
            options.add(StandardCopyOption.ATOMIC_MOVE);
        }
        if (mNoFollowLinks) {
            options.add(LinkOption.NOFOLLOW_LINKS);
        }
        if (mProgressListener != null) {
            options.add(new ProgressCopyOption(mProgressListener, mProgressIntervalMillis));
        }
        return options.toArray(new CopyOption[0]);
    }
}
