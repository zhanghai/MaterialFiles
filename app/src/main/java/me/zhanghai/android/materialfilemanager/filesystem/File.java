/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.content.Context;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import java.util.List;

import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.file.MimeTypes;
import me.zhanghai.android.materialfilemanager.util.CollectionUtils;

public interface File {

    enum Type {
        UNKNOWN,
        // Posix
        // https://www.gnu.org/software/libc/manual/html_node/Testing-File-Type.html
        DIRECTORY,
        CHARACTER_DEVICE,
        BLOCK_DEVICE,
        REGULAR_FILE,
        FIFO,
        SYMBOLIC_LINK,
        SOCKET
    }

    enum Permission {
        OWNER_READ,
        OWNER_WRITE,
        OWNER_EXECUTE,
        GROUP_READ,
        GROUP_WRITE,
        GROUP_EXECUTE,
        OTHERS_READ,
        OTHERS_WRITE,
        OTHERS_EXECUTE
    }

    @NonNull
    Uri getPath();

    @NonNull
    List<File> makeFilePath();

    @NonNull
    default String getName(Context context) {
        List<String> segments = getPath().getPathSegments();
        if (!segments.isEmpty()) {
            return CollectionUtils.last(segments);
        }
        return context.getString(R.string.file_name_root);
    }

    @WorkerThread
    void loadInformation();

    @NonNull
    String getDescription(Context context);

    @NonNull
    Type getType();

    default boolean isDirectory() {
        // TODO: Or archive directory.
        return getType() == Type.DIRECTORY;
    }

    @NonNull
    default String getMimeType() {
        if (isDirectory()) {
            return DocumentsContract.Document.MIME_TYPE_DIR;
        }
        return MimeTypes.getMimeType(getPath().toString());
    }

    boolean isListable();

    @WorkerThread
    void loadFileList();

    @NonNull
    List<File> getFileList();

    @NonNull
    java.io.File makeJavaFile();

    boolean equals(Object object);

    int hashCode();
}
