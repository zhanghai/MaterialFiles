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
import java.util.Objects;

import eu.chainfire.libsuperuser.Shell;
import me.zhanghai.android.materialfilemanager.functional.Functional;

public class LocalFile extends BaseFile {

    private Stat.Information mInformation;
    private boolean mIsArchive;

    public LocalFile(Uri path) {
        super(path);

        loadInformation();
        loadIsArchive();
    }

    private LocalFile(Uri path, Stat.Information information) {
        super(path);

        mInformation = information;
        loadIsArchive();
    }

    @NonNull
    @Override
    public Type getType() {
        return mInformation.type;
    }

    @Override
    public boolean isListable() {
        return mInformation.type == Type.DIRECTORY || mIsArchive;
    }

    @WorkerThread
    private void loadInformation() {
        String command = Stat.makeCommand(mPath.getPath());
        List<String> outputs = Shell.SH.run(command);
        if (outputs == null) {
            // TODO
        }
        String output = outputs.get(0);
        mInformation = Stat.parseOutput(output);
    }

    private void loadIsArchive() {
        // TODO
        mIsArchive = false;
    }

    @Override
    protected void onLoadFileList() {
        if (mInformation.type == Type.DIRECTORY) {
            loadDirectoryFileList();
        } else {
            loadArchiveFileList();
        }
    }

    @WorkerThread
    private void loadDirectoryFileList() {
        List<java.io.File> javaFiles = Arrays.asList(new java.io.File(mPath.getPath()).listFiles());
        List<String> paths = Functional.map(javaFiles, java.io.File::getPath);
        // TODO: ARG_MAX
        String command = Stat.makeCommand(paths);
        List<String> outputs = Shell.SH.run(command);
        if (outputs == null) {
            // TODO
        }
        List<Stat.Information> informations = Functional.map(outputs, Stat::parseOutput);
        List<File> files = Functional.map(javaFiles, (javaFile, index) -> new LocalFile(
                Uri.fromFile(javaFile), informations.get(index)));
        setFileList(files);
    }

    @WorkerThread
    private void loadArchiveFileList() {
        // TODO
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
        return Objects.equals(mPath, that.mPath) && Objects.equals(mInformation, that.mInformation)
                && mIsArchive == that.mIsArchive;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mPath, mInformation, mIsArchive);
    }
}
