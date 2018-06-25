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

public class JavaLocalFile extends LocalFile {

    private JavaFile.Information mInformation;

    public JavaLocalFile(Uri path) {
        super(path);
    }

    private JavaLocalFile(Uri path, JavaFile.Information information) {
        super(path);

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
    public List<File> loadFileList() throws FileSystemException {
        List<java.io.File> javaFiles = JavaFile.listFiles(makeJavaFile());
        List<JavaFile.Information> informations;
        try {
            informations = Functional.map(javaFiles, (ThrowingFunction<java.io.File,
                    JavaFile.Information>) JavaFile::loadInformation);
        } catch (FunctionalException e) {
            throw e.getCauseAs(FileSystemException.class);
        }
        return Functional.map(javaFiles, (javaFile, index) -> new JavaLocalFile(
                Uri.fromFile(javaFile), informations.get(index)));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        JavaLocalFile that = (JavaLocalFile) object;
        return Objects.equals(mPath, that.mPath)
                && Objects.equals(mInformation, that.mInformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mPath, mInformation);
    }


    public static final Creator<JavaLocalFile> CREATOR = new Creator<JavaLocalFile>() {
        @Override
        public JavaLocalFile createFromParcel(Parcel source) {
            return new JavaLocalFile(source);
        }
        @Override
        public JavaLocalFile[] newArray(int size) {
            return new JavaLocalFile[size];
        }
    };

    protected JavaLocalFile(Parcel in) {
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
