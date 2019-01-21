/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import me.zhanghai.android.files.provider.common.PosixFileAttributeView;
import me.zhanghai.android.files.provider.remote.RemotePosixFileAttributeView;

public class RemoteArchiveFileAttributeView
        extends RemotePosixFileAttributeView<ArchiveFileAttributes> {

    private static final String NAME = RemoteArchiveFileSystemProvider.SCHEME;

    static final Set<String> SUPPORTED_NAMES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("basic", "posix", NAME)));

    @NonNull
    private final Path mPath;

    public RemoteArchiveFileAttributeView(@NonNull Path path) {
        mPath = path;
    }

    @Override
    public String name() {
        return NAME;
    }

    @NonNull
    @Override
    public ArchiveFileAttributes readAttributes() throws IOException {
        RemoteArchiveFileSystemProvider.refreshFileSystemIfNeeded(mPath);

        return super.readAttributes();
    }

    @NonNull
    @Override
    public PosixFileAttributeView toLocal() {
        return new ArchiveFileAttributeView(mPath);
    }

    public static final Creator<RemoteArchiveFileAttributeView> CREATOR =
            new Creator<RemoteArchiveFileAttributeView>() {
                @Override
                public RemoteArchiveFileAttributeView createFromParcel(Parcel source) {
                    return new RemoteArchiveFileAttributeView(source);
                }
                @Override
                public RemoteArchiveFileAttributeView[] newArray(int size) {
                    return new RemoteArchiveFileAttributeView[size];
                }
            };

    protected RemoteArchiveFileAttributeView(Parcel in) {
        mPath = in.readParcelable(Path.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable((Parcelable) mPath, flags);
    }
}
