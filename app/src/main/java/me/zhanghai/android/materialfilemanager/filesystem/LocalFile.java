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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.filelist.PathHistory;
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
    public List<PathHistory.Segment> makePathSegments() {
        List<PathHistory.Segment> path = new ArrayList<>();
        java.io.File file = makeJavaFile();
        while (file != null) {
            path.add(new PathHistory.Segment(file.getName(), new LocalFile(Uri.fromFile(file))));
            file = file.getParentFile();
        }
        Collections.reverse(path);
        return path;
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

    @NonNull
    @Override
    public String getDescription(Context context) {
        if (mInformation.type == Type.DIRECTORY) {
            long subdirectoryCount = mInformation.hardLinkCount - 2;
            if (subdirectoryCount > 0) {
                int quantity = (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE,
                        subdirectoryCount));
                return context.getResources().getQuantityString(
                        R.plurals.file_description_directory_subdirectory_count_format, quantity,
                        subdirectoryCount);
            } else {
                return context.getString(R.string.file_description_directory);
            }
        } else {
            return Formatter.formatFileSize(context, mInformation.size);
        }
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

    @NonNull
    @Override
    public java.io.File makeJavaFile() {
        return new java.io.File(mPath.getPath());
    }
}
