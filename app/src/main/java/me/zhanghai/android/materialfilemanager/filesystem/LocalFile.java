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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class LocalFile extends BaseFile {

    public static final String SCHEME = "file";

    private LocalFileStrategy mStrategy;

    private transient JavaFileObserver mObserver;

    static Uri uriFromPath(String path) {
        return new Uri.Builder()
                .scheme(SCHEME)
                .path(path)
                .build();
    }

    static String uriToPath(Uri uri) {
        return uri.getPath();
    }

    static String joinPaths(String parent, String child) {
        return new java.io.File(parent, child).getPath();
    }

    public LocalFile(Uri uri, LocalFileStrategy strategy) {
        super(uri);

        mStrategy = strategy;
    }

    public LocalFile(Uri uri) {
        // TODO
        this(uri, new LocalFileStrategies.AndroidOsStrategy());
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
        mStrategy.reloadInformation(this);
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
    public boolean isDirectory() {
        return mStrategy.isDirectory();
    }

    @Override
    public boolean isSymbolicLink() {
        return mStrategy.isSymbolicLink();
    }

    @Override
    public List<File> getChildren() throws FileSystemException {
        return mStrategy.getChildren(this);
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
    public boolean equals(Object object) {
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

    @Override
    public int hashCode() {
        return Objects.hash(mUri, mStrategy);
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
