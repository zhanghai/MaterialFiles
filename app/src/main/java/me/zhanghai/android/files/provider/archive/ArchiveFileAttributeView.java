/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Set;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import me.zhanghai.android.files.provider.root.RootablePosixFileAttributeView;

public class ArchiveFileAttributeView extends RootablePosixFileAttributeView {

    static final Set<String> SUPPORTED_NAMES = LocalArchiveFileAttributeView.SUPPORTED_NAMES;

    @NonNull
    private final Path mPath;

    ArchiveFileAttributeView(@NonNull Path path) {
        super(path, new LocalArchiveFileAttributeView(path), attributeView ->
                new RootArchiveFileAttributeView(attributeView, path));

        mPath = path;
    }


    public static final Creator<ArchiveFileAttributeView> CREATOR =
            new Creator<ArchiveFileAttributeView>() {
                @Override
                public ArchiveFileAttributeView createFromParcel(Parcel source) {
                    return new ArchiveFileAttributeView(source);
                }
                @Override
                public ArchiveFileAttributeView[] newArray(int size) {
                    return new ArchiveFileAttributeView[size];
                }
            };

    protected ArchiveFileAttributeView(Parcel in) {
        this((Path) in.readParcelable(Path.class.getClassLoader()));
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
