/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.WorkerThread;

import org.threeten.bp.Instant;

import java.util.List;
import java.util.Objects;

import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.functional.FunctionalException;
import me.zhanghai.android.materialfilemanager.functional.throwing.ThrowingFunction;

public class AndroidOsLocalFile extends LocalFile {

    private AndroidOs.Information mInformation;

    private JavaFileObserver mObserver;

    public AndroidOsLocalFile(Uri uri) {
        super(uri);
    }

    private AndroidOsLocalFile(Uri uri, AndroidOs.Information information) {
        super(uri);

        mInformation = information;
    }

    @WorkerThread
    public void loadInformation() throws FileSystemException {
        mInformation = AndroidOs.loadInformation(mUri.getPath());
    }

    @Override
    public long getSize() {
        return mInformation.size;
    }

    @Override
    public Instant getLastModified() {
        return mInformation.lastModificationTime;
    }

    @Override
    public boolean isDirectory() {
        // TODO: Symbolic link?
        return mInformation.type == PosixFileType.DIRECTORY;
    }

    @Override
    @WorkerThread
    public List<File> getChildren() throws FileSystemException {
        List<String> children = JavaFile.list(makeJavaFile());
        List<AndroidOs.Information> informations;
        try {
            informations = Functional.map(children, (ThrowingFunction<String,
                    AndroidOs.Information>) AndroidOs::loadInformation);
        } catch (FunctionalException e) {
            throw e.getCauseAs(FileSystemException.class);
        }
        return Functional.map(children, (child, index) -> new AndroidOsLocalFile(
                uriFromPath(child), informations.get(index)));
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
        AndroidOsLocalFile that = (AndroidOsLocalFile) object;
        return Objects.equals(mUri, that.mUri)
                && Objects.equals(mInformation, that.mInformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mUri, mInformation);
    }


    public static final Creator<AndroidOsLocalFile> CREATOR = new Creator<AndroidOsLocalFile>() {
        @Override
        public AndroidOsLocalFile createFromParcel(Parcel source) {
            return new AndroidOsLocalFile(source);
        }
        @Override
        public AndroidOsLocalFile[] newArray(int size) {
            return new AndroidOsLocalFile[size];
        }
    };

    protected AndroidOsLocalFile(Parcel in) {
        super(in);

        mInformation = in.readParcelable(AndroidOs.Information.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeParcelable(mInformation, flags);
    }
}
