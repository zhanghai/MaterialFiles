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

public class JavaFileLocalFile extends LocalFile {

    private JavaFile.Information mInformation;

    private JavaFileObserver mObserver;

    public JavaFileLocalFile(Uri uri) {
        super(uri);
    }

    private JavaFileLocalFile(Uri uri, JavaFile.Information information) {
        super(uri);

        mInformation = information;
    }

    @WorkerThread
    public void loadInformation() throws FileSystemException {
        mInformation = JavaFile.loadInformation(makeJavaFile());
    }

    @Override
    public long getSize() {
        return mInformation.length;
    }

    @Override
    public Instant getLastModified() {
        return mInformation.lastModified;
    }

    @Override
    public boolean isDirectory() {
        return mInformation.isDirectory;
    }

    @Override
    @WorkerThread
    public List<File> getChildren() throws FileSystemException {
        List<java.io.File> javaFiles = JavaFile.getChildFiles(makeJavaFile());
        List<JavaFile.Information> informations;
        try {
            informations = Functional.map(javaFiles, (ThrowingFunction<java.io.File,
                    JavaFile.Information>) JavaFile::loadInformation);
        } catch (FunctionalException e) {
            throw e.getCauseAs(FileSystemException.class);
        }
        return Functional.map(javaFiles, (javaFile, index) -> new JavaFileLocalFile(
                Uri.fromFile(javaFile), informations.get(index)));
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
        JavaFileLocalFile that = (JavaFileLocalFile) object;
        return Objects.equals(mUri, that.mUri)
                && Objects.equals(mInformation, that.mInformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mUri, mInformation);
    }


    public static final Creator<JavaFileLocalFile> CREATOR = new Creator<JavaFileLocalFile>() {
        @Override
        public JavaFileLocalFile createFromParcel(Parcel source) {
            return new JavaFileLocalFile(source);
        }
        @Override
        public JavaFileLocalFile[] newArray(int size) {
            return new JavaFileLocalFile[size];
        }
    };

    protected JavaFileLocalFile(Parcel in) {
        super(in);

        mInformation = in.readParcelable(JavaFile.Information.class.getClassLoader());
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
