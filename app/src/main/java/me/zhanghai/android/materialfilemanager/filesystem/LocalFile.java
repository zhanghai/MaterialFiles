/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.util.Arrays;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;
import me.zhanghai.android.materialfilemanager.functional.Functional;

public class LocalFile extends BaseFile<Stat.Information> {

    public LocalFile(Uri path) {
        super(path);
    }

    private LocalFile(Uri path, Stat.Information information) {
        super(path);

        mInformation = information;
    }

    @NonNull
    @Override
    public Type getType() {
        return mInformation.type;
    }

    @Override
    public boolean isListable() {
        // TODO
        return mInformation.type == Type.DIRECTORY/* || mIsArchive*/;
    }

    @WorkerThread
    public void loadInformation() {
        String command = Stat.makeCommand(mPath.getPath());
        List<String> outputs = Shell.SH.run(command);
        if (outputs == null) {
            // TODO
        }
        mInformation = Stat.parseOutput(outputs.get(0));
    }

    @Override
    public void loadFileList() {
        boolean isDirectory;
        if (mInformation != null) {
            isDirectory = mInformation.type == Type.DIRECTORY;
        } else {
            isDirectory = makeJavaFile().isDirectory();
        }
        if (isDirectory) {
            loadDirectoryFileList();
        } else {
            loadArchiveFileList();
        }
    }

    @WorkerThread
    private void loadDirectoryFileList() {
        List<java.io.File> javaFiles = Arrays.asList(makeJavaFile().listFiles());
        List<String> paths = Functional.map(javaFiles, java.io.File::getPath);
        // TODO: ARG_MAX
        String command = Stat.makeCommand(paths);
        List<String> outputs = Shell.SH.run(command);
        if (outputs == null) {
            // TODO
        }
        List<Stat.Information> informations = Functional.map(outputs, Stat::parseOutput);
        mFileList = Functional.map(javaFiles, (javaFile, index) -> new LocalFile(
                Uri.fromFile(javaFile), informations.get(index)));
    }

    @WorkerThread
    private void loadArchiveFileList() {
        // TODO
    }

    private java.io.File makeJavaFile() {
        return new java.io.File(mPath.getPath());
    }
}
