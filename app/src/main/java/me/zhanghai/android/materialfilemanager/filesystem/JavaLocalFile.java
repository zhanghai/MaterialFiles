/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.WorkerThread;

import org.threeten.bp.Instant;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.functional.Functional;

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
    public void loadInformation() {
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
        java.io.File[] javaFileArray = makeJavaFile().listFiles();
        if (javaFileArray == null) {
            throw new FileSystemException(R.string.error_unable_to_open_directory);
        }
        List<java.io.File> javaFiles = Arrays.asList(javaFileArray);
        List<JavaFile.Information> informations = Functional.map(javaFiles,
                JavaFile::loadInformation);
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
    }
}
