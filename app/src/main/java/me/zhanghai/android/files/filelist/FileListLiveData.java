/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import java8.nio.file.DirectoryStream;
import java8.nio.file.Files;
import java8.nio.file.LinkOption;
import java8.nio.file.Path;
import java8.nio.file.attribute.BasicFileAttributes;
import me.zhanghai.android.files.filesystem.JavaFileObserver;
import me.zhanghai.android.files.functional.FunctionalException;
import me.zhanghai.android.files.functional.FunctionalIterator;
import me.zhanghai.android.files.functional.throwing.ThrowingFunction;
import me.zhanghai.android.files.provider.common.AndroidFileTypeDetector;
import me.zhanghai.android.files.provider.linux.LinuxFileSystemProvider;

public class FileListLiveData extends LiveData<FileListData> {

    @NonNull
    private final Path mPath;

    @Nullable
    private JavaFileObserver mFileObserver;

    public FileListLiveData(@NonNull Path path) {
        mPath = path;
        loadValue();
    }

    @SuppressLint("StaticFieldLeak")
    private void loadValue() {
        setValue(FileListData.ofLoading(mPath));
        new AsyncTask<Void, Void, FileListData>() {
            @Override
            @WorkerThread
            protected FileListData doInBackground(Void... parameters) {
                try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(mPath)) {
                    List<FileItem> fileList;
                    try {
                        fileList = FunctionalIterator.mapRemaining(directoryStream.iterator(),
                                (ThrowingFunction<Path, FileItem>) FileListLiveData::loadFileItem);
                    } catch (FunctionalException e) {
                        throw e.getCauseAs(IOException.class);
                    }
                    return FileListData.ofSuccess(mPath, fileList);
                } catch (Exception e) {
                    return FileListData.ofError(mPath, e);
                }
            }
            @Override
            protected void onPostExecute(FileListData fileListData) {
                setValue(fileListData);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @NonNull
    @WorkerThread
    private static FileItem loadFileItem(@NonNull Path path) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class,
                LinkOption.NOFOLLOW_LINKS);
        boolean hidden = Files.isHidden(path);
        if (!attributes.isSymbolicLink()) {
            String mimeType = AndroidFileTypeDetector.getMimeType(path, attributes);
            return new FileItem(path, attributes, null, null, hidden, mimeType);
        }
        String symbolicLinkTarget = Files.readSymbolicLink(path).toString();
        BasicFileAttributes symbolicLinkTargetAttributes;
        try {
            symbolicLinkTargetAttributes = Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e) {
            e.printStackTrace();
            symbolicLinkTargetAttributes = null;
        }
        String mimeType = AndroidFileTypeDetector.getMimeType(path,
                symbolicLinkTargetAttributes != null ? symbolicLinkTargetAttributes : attributes);
        return new FileItem(path, attributes, symbolicLinkTarget, symbolicLinkTargetAttributes,
                hidden, mimeType);
    }

    @Override
    protected void onActive() {
        if (LinuxFileSystemProvider.isLinuxPath(mPath)) {
            mFileObserver = new JavaFileObserver(mPath.toFile().getPath(), this::loadValue);
            mFileObserver.startWatching();
        }
    }

    @Override
    protected void onInactive() {
        if (mFileObserver != null) {
            mFileObserver.stopWatching();
            mFileObserver = null;
        }
    }
}
