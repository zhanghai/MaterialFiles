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

import eu.chainfire.libsuperuser.Shell;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.functional.Functional;

public class ShellLocalFile extends LocalFile {

    private Stat.Information mInformation;

    public ShellLocalFile(Uri path) {
        super(path);
    }

    private ShellLocalFile(Uri path, Stat.Information information) {
        super(path);

        mInformation = information;
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
    public long getSize() {
        return mInformation.size;
    }

    @Override
    public Instant getModified() {
        return mInformation.lastModification;
    }

    @NonNull
    public PosixFileType getType() {
        return mInformation.type;
    }

    @Override
    public boolean isDirectory() {
        return mInformation.type == PosixFileType.DIRECTORY;
    }

    @Override
    @WorkerThread
    public void loadFileList() {
        List<java.io.File> javaFiles = Arrays.asList(makeJavaFile().listFiles());
        List<String> paths = Functional.map(javaFiles, java.io.File::getPath);
        // TODO: ARG_MAX
        String command = Stat.makeCommand(paths);
        List<String> outputs = Shell.SH.run(command);
        if (outputs == null) {
            // TODO
        }
        List<Stat.Information> informations = Functional.map(outputs, Stat::parseOutput);
        mFileList = Functional.map(javaFiles, (javaFile, index) -> new ShellLocalFile(
                Uri.fromFile(javaFile), informations.get(index)));
    }
}
