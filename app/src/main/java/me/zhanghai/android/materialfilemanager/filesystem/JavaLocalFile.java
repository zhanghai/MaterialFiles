/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.text.format.Formatter;

import org.threeten.bp.Instant;

import java.util.Arrays;
import java.util.List;

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
    public Instant getModified() {
        return mInformation.lastModified;
    }

    @Override
    public boolean isDirectory() {
        return mInformation.isDirectory;
    }

    @Override
    @WorkerThread
    public void loadFileList() {
        List<java.io.File> javaFiles = Arrays.asList(makeJavaFile().listFiles());
        List<JavaFile.Information> informations = Functional.map(javaFiles,
                JavaFile::loadInformation);
        mFileList = Functional.map(javaFiles, (javaFile, index) -> new JavaLocalFile(
                Uri.fromFile(javaFile), informations.get(index)));
    }
}
