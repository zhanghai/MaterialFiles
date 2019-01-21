/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import android.os.Parcel;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.provider.common.PosixFileAttributeView;
import me.zhanghai.android.files.provider.remote.RemotePosixFileAttributeView;

public class RemoteLinuxFileAttributeView
        extends RemotePosixFileAttributeView<LinuxFileAttributes> {

    private static final String NAME = RemoteLinuxFileSystemProvider.SCHEME;

    static final Set<String> SUPPORTED_NAMES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("basic", "posix", NAME)));

    @NonNull
    private final String mPath;
    private final boolean mNoFollowLinks;

    RemoteLinuxFileAttributeView(@NonNull String path, boolean noFollowLinks) {
        mPath = path;
        mNoFollowLinks = noFollowLinks;
    }

    @Override
    public String name() {
        return NAME;
    }

    @NonNull
    @Override
    public PosixFileAttributeView toLocal() {
        return new LinuxFileAttributeView(mPath, mNoFollowLinks);
    }


    public static final Creator<RemoteLinuxFileAttributeView> CREATOR =
            new Creator<RemoteLinuxFileAttributeView>() {
                @Override
                public RemoteLinuxFileAttributeView createFromParcel(Parcel source) {
                    return new RemoteLinuxFileAttributeView(source);
                }
                @Override
                public RemoteLinuxFileAttributeView[] newArray(int size) {
                    return new RemoteLinuxFileAttributeView[size];
                }
            };

    protected RemoteLinuxFileAttributeView(Parcel in) {
        mPath = in.readString();
        mNoFollowLinks = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPath);
        dest.writeByte(mNoFollowLinks ? (byte) 1 : (byte) 0);
    }
}
