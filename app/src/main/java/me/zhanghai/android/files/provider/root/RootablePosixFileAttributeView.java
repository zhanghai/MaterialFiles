/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.root;

import android.os.Parcelable;

import java.io.IOException;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import java8.nio.file.attribute.FileTime;
import java9.util.function.Function;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.PosixFileAttributeView;
import me.zhanghai.android.files.provider.common.PosixFileAttributes;
import me.zhanghai.android.files.provider.common.PosixFileModeBit;
import me.zhanghai.android.files.provider.common.PosixGroup;
import me.zhanghai.android.files.provider.common.PosixUser;

public abstract class RootablePosixFileAttributeView implements PosixFileAttributeView, Parcelable {

    @NonNull
    private final Path mPath;
    @NonNull
    private final PosixFileAttributeView mLocalAttributeView;
    @NonNull
    private final RootPosixFileAttributeView mRootAttributeView;

    public RootablePosixFileAttributeView(
            @NonNull Path path, @NonNull PosixFileAttributeView localAttributeView,
            @NonNull Function<PosixFileAttributeView, RootPosixFileAttributeView>
                    newRootAttributeView) {
        mPath = path;
        mLocalAttributeView = localAttributeView;
        mRootAttributeView = newRootAttributeView.apply(this);
    }

    @Override
    public String name() {
        return mLocalAttributeView.name();
    }

    @Override
    public void setTimes(@Nullable FileTime lastModifiedTime, @Nullable FileTime lastAccessTime,
                         @Nullable FileTime createTime) throws IOException {
        acceptRootable(mPath, attributeView -> attributeView.setTimes(lastModifiedTime,
                lastAccessTime, createTime));
    }

    @NonNull
    @Override
    public PosixFileAttributes readAttributes() throws IOException {
        return applyRootable(mPath, PosixFileAttributeView::readAttributes);
    }

    @Override
    public void setOwner(@NonNull PosixUser owner) throws IOException {
        acceptRootable(mPath, attributeView -> attributeView.setOwner(owner));
    }

    @Override
    public void setGroup(@NonNull PosixGroup group) throws IOException {
        acceptRootable(mPath, attributeView -> attributeView.setGroup(group));
    }

    @Override
    public void setMode(@NonNull Set<PosixFileModeBit> mode) throws IOException {
        acceptRootable(mPath, attributeView -> attributeView.setMode(mode));
    }

    @Override
    public void setSeLinuxContext(@NonNull ByteString context) throws IOException {
        acceptRootable(mPath, attributeView -> attributeView.setSeLinuxContext(context));
    }

    @Override
    public void restoreSeLinuxContext() throws IOException {
        acceptRootable(mPath, PosixFileAttributeView::restoreSeLinuxContext);
    }

    private void acceptRootable(@NonNull Path path,
                                RootUtils.Consumer<PosixFileAttributeView> consumer)
            throws IOException {
        RootUtils.acceptRootable(path, mLocalAttributeView, mRootAttributeView, consumer);
    }

    private <R> R applyRootable(@NonNull Path path,
                                RootUtils.Function<PosixFileAttributeView, R> function)
            throws IOException {
        return RootUtils.applyRootable(path, mLocalAttributeView, mRootAttributeView, function);
    }
}
