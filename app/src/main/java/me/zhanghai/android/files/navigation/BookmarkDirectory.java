/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import me.zhanghai.android.files.filelist.FileUtils;

public class BookmarkDirectory implements Parcelable {

    private final long mId;

    @Nullable
    private final String mName;

    @NonNull
    private final Path mPath;

    private BookmarkDirectory(long id, @Nullable String name, @NonNull Path path) {
        mId = id;
        mName = name;
        mPath = path;
    }

    public BookmarkDirectory(@Nullable String name, @NonNull Path path) {
        // We cannot simply use path.hashCode() as ID because different bookmark directories may
        // have the same path.
        this(new Random().nextLong(), name, path);
    }

    public BookmarkDirectory(@NonNull BookmarkDirectory bookmarkDirectory, @Nullable String name,
                             @NonNull Path path) {
        this(bookmarkDirectory.mId, name, path);
    }

    public long getId() {
        return mId;
    }

    @NonNull
    public String getName() {
        if (!TextUtils.isEmpty(mName)) {
            return mName;
        }
        return FileUtils.getName(mPath);
    }

    @NonNull
    public Path getPath() {
        return mPath;
    }


    public static final Creator<BookmarkDirectory> CREATOR = new Creator<BookmarkDirectory>() {
        @Override
        public BookmarkDirectory createFromParcel(Parcel source) {
            return new BookmarkDirectory(source);
        }
        @Override
        public BookmarkDirectory[] newArray(int size) {
            return new BookmarkDirectory[size];
        }
    };

    protected BookmarkDirectory(Parcel in) {
        mId = in.readLong();
        mName = in.readString();
        mPath = in.readParcelable(Path.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mName);
        dest.writeParcelable((Parcelable) mPath, flags);
    }
}
