/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.threeten.bp.Instant;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import me.zhanghai.android.materialfilemanager.file.MimeTypes;

public class LocalFile extends BaseFile {

    public static final String SCHEME = "file";

    @NonNull
    private LocalFileStrategy mStrategy;

    @NonNull
    static Uri uriFromPath(@NonNull String path) {
        return new Uri.Builder()
                .scheme(SCHEME)
                .authority("")
                .path(path)
                .build();
    }

    @NonNull
    static String uriToPath(@NonNull Uri uri) {
        return uri.getPath();
    }

    @NonNull
    static String joinPaths(@NonNull String parent, @NonNull String child) {
        return new java.io.File(parent, child).getPath();
    }

    LocalFile(@NonNull Uri uri, @NonNull LocalFileStrategy strategy) {
        super(uri);

        mStrategy = strategy;
    }

    public LocalFile(@NonNull Uri uri) {
        // TODO: Better determination of strategy?
        this(uri, new LocalFileStrategies.SyscallStrategy());
    }

    @NonNull
    public String getPath() {
        return uriToPath(mUri);
    }

    @Nullable
    @Override
    public LocalFile getParent() {
        String parentPath = new java.io.File(getPath()).getParent();
        if (TextUtils.isEmpty(parentPath)) {
            return null;
        }
        return new LocalFile(uriFromPath(parentPath));
    }

    @NonNull
    @Override
    public LocalFile getChild(@NonNull String childName) {
        String childPath = new java.io.File(getPath(), childName).getPath();
        return new LocalFile(uriFromPath(childPath));
    }

    @Override
    public boolean hasInformation() {
        return mStrategy.hasInformation();
    }

    @Override
    public void reloadInformation() throws FileSystemException {
        try {
            mStrategy.reloadInformation(this);
        } catch (FileSystemException e) {
            // TODO: Better determination of strategy beforehand?
            if (mStrategy instanceof LocalFileStrategies.ShellFsStrategy) {
                throw e;
            }
            e.printStackTrace();
            mStrategy = new LocalFileStrategies.ShellFsStrategy();
            reloadInformation();
        }
    }

    @Override
    public boolean isSymbolicLink() {
        return mStrategy.isSymbolicLink();
    }

    @Override
    public boolean isSymbolicLinkBroken() {
        return mStrategy.isSymbolicLinkBroken();
    }

    @NonNull
    @Override
    public String getSymbolicLinkTarget() {
        return mStrategy.getSymbolicLinkTarget();
    }

    @NonNull
    public PosixFileType getType() {
        return mStrategy.getType();
    }

    @Override
    public boolean isDirectory() {
        return getType() == PosixFileType.DIRECTORY;
    }

    @NonNull
    @Override
    public String getMimeType() {
        String mimeType = MimeTypes.getPosixMimeType(getType());
        if (!TextUtils.isEmpty(mimeType)) {
            return mimeType;
        }
        return super.getMimeType();
    }

    @NonNull
    public Set<PosixFileModeBit> getMode() {
        return mStrategy.getMode();
    }

    @NonNull
    public PosixUser getOwner() {
        return mStrategy.getOwner();
    }

    @NonNull
    public PosixGroup getGroup() {
        return mStrategy.getGroup();
    }

    @Override
    public long getSize() {
        return mStrategy.getSize();
    }

    @NonNull
    @Override
    public Instant getLastModificationTime() {
        return mStrategy.getLastModificationTime();
    }

    @NonNull
    @Override
    public List<File> getChildren() throws FileSystemException {
        try {
            return mStrategy.getChildren(this);
        } catch (FileSystemException e) {
            // TODO: Better determination of strategy beforehand?
            if (mStrategy instanceof LocalFileStrategies.ShellFsStrategy) {
                throw e;
            }
            e.printStackTrace();
            mStrategy = new LocalFileStrategies.ShellFsStrategy();
            return getChildren();
        }
    }


    @Override
    public boolean equalsIncludingInformation(@Nullable Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        LocalFile that = (LocalFile) object;
        return Objects.equals(mUri, that.mUri)
                && Objects.equals(mStrategy, that.mStrategy);
    }


    public static final Creator<LocalFile> CREATOR = new Creator<LocalFile>() {
        @NonNull
        @Override
        public LocalFile createFromParcel(Parcel source) {
            return new LocalFile(source);
        }
        @NonNull
        @Override
        public LocalFile[] newArray(int size) {
            return new LocalFile[size];
        }
    };

    protected LocalFile(@NonNull Parcel in) {
        super(in);

        mStrategy = in.readParcelable(LocalFileStrategy.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeParcelable(mStrategy, flags);
    }
}
