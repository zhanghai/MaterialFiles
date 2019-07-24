/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.content;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import java8.nio.file.attribute.BasicFileAttributeView;
import java8.nio.file.attribute.FileTime;
import me.zhanghai.android.files.provider.content.resolver.Resolver;
import me.zhanghai.android.files.provider.content.resolver.ResolverException;

public class ContentFileAttributeView implements BasicFileAttributeView, Parcelable {

    private static final String NAME = ContentFileSystemProvider.SCHEME;

    static final Set<String> SUPPORTED_NAMES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("basic", NAME)));

    @NonNull
    private final ContentPath mPath;

    ContentFileAttributeView(@NonNull ContentPath path) {
        mPath = path;
    }

    @NonNull
    @Override
    public String name() {
        return NAME;
    }

    @NonNull
    @Override
    public ContentFileAttributes readAttributes() throws IOException {
        Uri uri = mPath.getUri();
        String mimeType;
        try {
            mimeType = Resolver.getMimeType(uri);
        } catch (ResolverException e) {
            throw e.toFileSystemException(mPath.toString());
        }
        long size;
        try {
            size = Resolver.getSize(uri);
        } catch (ResolverException e) {
            throw e.toFileSystemException(mPath.toString());
        }
        return new ContentFileAttributes(mimeType, size, uri);
    }

    @Override
    public void setTimes(@Nullable FileTime lastModifiedTime, @Nullable FileTime lastAccessTime,
                         @Nullable FileTime createTime) {
        throw new UnsupportedOperationException();
    }


    public static final Creator<ContentFileAttributeView> CREATOR =
            new Creator<ContentFileAttributeView>() {
                @Override
                public ContentFileAttributeView createFromParcel(Parcel source) {
                    return new ContentFileAttributeView(source);
                }
                @Override
                public ContentFileAttributeView[] newArray(int size) {
                    return new ContentFileAttributeView[size];
                }
            };

    protected ContentFileAttributeView(Parcel in) {
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
