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

    private LocalFileStrategy mStrategy;

    private transient JavaFileObserver mObserver;

    static Uri uriFromPath(String path) {
        return new Uri.Builder()
                .scheme(SCHEME)
                .authority("")
                .path(path)
                .build();
    }

    static String uriToPath(Uri uri) {
        return uri.getPath();
    }

    static String joinPaths(String parent, String child) {
        return new java.io.File(parent, child).getPath();
    }

    LocalFile(Uri uri, LocalFileStrategy strategy) {
        super(uri);

        mStrategy = strategy;
    }

    public LocalFile(Uri uri) {
        // TODO: Better determination of strategy?
        this(uri, new LocalFileStrategies.SyscallStrategy());
    }

    public String getPath() {
        return uriToPath(mUri);
    }

    public java.io.File makeJavaFile() {
        return new java.io.File(getPath());
    }

    @Nullable
    @Override
    public LocalFile getParent() {
        String parentPath = makeJavaFile().getParent();
        if (TextUtils.isEmpty(parentPath)) {
            return null;
        }
        return new LocalFile(uriFromPath(parentPath));
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

    @Override
    public String getSymbolicLinkTarget() {
        return mStrategy.getSymbolicLinkTarget();
    }

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

    public Set<PosixFileModeBit> getMode() {
        return mStrategy.getMode();
    }

    public PosixUser getOwner() {
        return mStrategy.getOwner();
    }

    public PosixGroup getGroup() {
        return mStrategy.getGroup();
    }

    @Override
    public long getSize() {
        return mStrategy.getSize();
    }

    @Override
    public Instant getLastModificationTime() {
        return mStrategy.getLastModificationTime();
    }

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
    public void startObserving(Runnable observer) {
        if (mObserver != null) {
            throw new IllegalStateException("Already observing");
        }
        mObserver = new JavaFileObserver(mUri.getPath(), observer);
        mObserver.startWatching();
    }

    @Override
    public boolean isObserving() {
        return mObserver != null;
    }

    @Override
    public void stopObserving() {
        if (mObserver != null) {
            mObserver.stopWatching();
            mObserver = null;
        }
    }


    @Override
    public boolean equalsIncludingInformation(Object object) {
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
        @Override
        public LocalFile createFromParcel(Parcel source) {
            return new LocalFile(source);
        }
        @Override
        public LocalFile[] newArray(int size) {
            return new LocalFile[size];
        }
    };

    protected LocalFile(Parcel in) {
        super(in);

        mStrategy = in.readParcelable(LocalFileStrategy.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeParcelable(mStrategy, flags);
    }
}
