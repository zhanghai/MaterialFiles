/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import java8.nio.file.Files;
import java8.nio.file.LinkOption;
import java8.nio.file.Path;
import java8.nio.file.attribute.BasicFileAttributes;
import me.zhanghai.android.files.provider.common.AndroidFileTypeDetector;
import me.zhanghai.android.files.provider.common.MoreFiles;

public class FileItem implements Parcelable {

    @NonNull
    private final Path mPath;
    @NonNull
    private final BasicFileAttributes mAttributes;
    @Nullable
    private final String mSymbolicLinkTarget;
    @Nullable
    private final BasicFileAttributes mSymbolicLinkTargetAttributes;
    private final boolean mHidden;
    @NonNull
    private final String mMimeType;

    public FileItem(@NonNull Path path, @NonNull BasicFileAttributes attributes,
                    @Nullable String symbolicLinkTarget,
                    @Nullable BasicFileAttributes symbolicLinkTargetAttributes,
                    boolean hidden, @NonNull String mimeType) {
        mPath = path;
        mAttributes = attributes;
        mSymbolicLinkTarget = symbolicLinkTarget;
        mSymbolicLinkTargetAttributes = symbolicLinkTargetAttributes;
        mHidden = hidden;
        mMimeType = mimeType;
    }

    protected FileItem(@NonNull FileItem fileItem) {
        this(fileItem.mPath, fileItem.mAttributes, fileItem.mSymbolicLinkTarget,
                fileItem.mSymbolicLinkTargetAttributes, fileItem.mHidden, fileItem.mMimeType);
    }

    @NonNull
    @WorkerThread
    public static FileItem load(@NonNull Path path) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class,
                LinkOption.NOFOLLOW_LINKS);
        boolean hidden = Files.isHidden(path);
        if (!attributes.isSymbolicLink()) {
            String mimeType = AndroidFileTypeDetector.getMimeType(path, attributes);
            return new FileItem(path, attributes, null, null, hidden, mimeType);
        }
        String symbolicLinkTarget = MoreFiles.readSymbolicLink(path).toString();
        BasicFileAttributes symbolicLinkTargetAttributes;
        try {
            symbolicLinkTargetAttributes = Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e) {
            e.printStackTrace();
            symbolicLinkTargetAttributes = null;
        }
        String mimeType = AndroidFileTypeDetector.getMimeType(path,
                symbolicLinkTargetAttributes != null ? symbolicLinkTargetAttributes : attributes);
        return new FileItem(path, attributes, symbolicLinkTarget, symbolicLinkTargetAttributes,
                hidden, mimeType);
    }

    @NonNull
    public Path getPath() {
        return mPath;
    }

    @NonNull
    public BasicFileAttributes getAttributes() {
        return mSymbolicLinkTargetAttributes != null ? mSymbolicLinkTargetAttributes : mAttributes;
    }

    @NonNull
    public BasicFileAttributes getAttributesNoFollowLinks() {
        return mAttributes;
    }

    public boolean isSymbolicLinkBroken() {
        if (!mAttributes.isSymbolicLink()) {
            throw new IllegalStateException("Not a symbolic link");
        }
        return mSymbolicLinkTargetAttributes == null;
    }

    @Nullable
    public String getSymbolicLinkTarget() {
        return mSymbolicLinkTarget;
    }

    public boolean isHidden() {
        return mHidden;
    }

    @NonNull
    public String getMimeType() {
        return mMimeType;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        FileItem fileItem = (FileItem) object;
        return Objects.equals(mPath, fileItem.mPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mPath);
    }

    public boolean contentEquals(@Nullable FileItem fileItem) {
        if (fileItem == null) {
            return false;
        }
        return mHidden == fileItem.mHidden
                && Objects.equals(mPath, fileItem.mPath)
                && Objects.equals(mAttributes, fileItem.mAttributes)
                && Objects.equals(mSymbolicLinkTarget, fileItem.mSymbolicLinkTarget)
                && Objects.equals(mSymbolicLinkTargetAttributes,
                fileItem.mSymbolicLinkTargetAttributes)
                && Objects.equals(mMimeType, fileItem.mMimeType);
    }

    public static boolean contentEquals(@Nullable FileItem fileItem1,
                                        @Nullable FileItem fileItem2) {
        return fileItem1 == fileItem2 || (fileItem1 != null && fileItem1.contentEquals(fileItem2));
    }


    public static final Creator<FileItem> CREATOR = new Creator<FileItem>() {
        @Override
        public FileItem createFromParcel(Parcel source) {
            return new FileItem(source);
        }
        @Override
        public FileItem[] newArray(int size) {
            return new FileItem[size];
        }
    };

    protected FileItem(Parcel in) {
        mPath = in.readParcelable(Path.class.getClassLoader());
        mAttributes = in.readParcelable(BasicFileAttributes.class.getClassLoader());
        mSymbolicLinkTarget = in.readString();
        mSymbolicLinkTargetAttributes = in.readParcelable(
                BasicFileAttributes.class.getClassLoader());
        mHidden = in.readByte() != 0;
        mMimeType = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable((Parcelable) mPath, flags);
        dest.writeParcelable((Parcelable) mAttributes, flags);
        dest.writeString(mSymbolicLinkTarget);
        dest.writeParcelable((Parcelable) mSymbolicLinkTargetAttributes, flags);
        dest.writeByte(mHidden ? (byte) 1 : (byte) 0);
        dest.writeString(mMimeType);
    }
}
