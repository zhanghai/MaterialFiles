/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import android.os.Parcel;

import java.util.Set;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import me.zhanghai.android.files.provider.root.RootPosixFileAttributeView;
import me.zhanghai.android.files.provider.root.RootablePosixFileAttributeView;

public class LinuxFileAttributeView extends RootablePosixFileAttributeView {

    static final Set<String> SUPPORTED_NAMES = LocalLinuxFileAttributeView.SUPPORTED_NAMES;

    @NonNull
    private final LinuxPath mPath;
    private final boolean mNoFollowLinks;

    LinuxFileAttributeView(@NonNull LinuxPath path, boolean noFollowLinks) {
        super(path, new LocalLinuxFileAttributeView(path.toByteString(), noFollowLinks),
                RootPosixFileAttributeView::new);

        mPath = path;
        mNoFollowLinks = noFollowLinks;
    }


    public static final Creator<LinuxFileAttributeView> CREATOR =
            new Creator<LinuxFileAttributeView>() {
                @Override
                public LinuxFileAttributeView createFromParcel(Parcel source) {
                    return new LinuxFileAttributeView(source);
                }
                @Override
                public LinuxFileAttributeView[] newArray(int size) {
                    return new LinuxFileAttributeView[size];
                }
            };

    protected LinuxFileAttributeView(Parcel in) {
        this(in.readParcelable(Path.class.getClassLoader()), in.readByte() != 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mPath, flags);
        dest.writeByte(mNoFollowLinks ? (byte) 1 : (byte) 0);
    }
}
