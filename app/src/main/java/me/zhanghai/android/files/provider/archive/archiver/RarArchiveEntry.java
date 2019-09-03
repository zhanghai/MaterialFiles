/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive.archiver;

import android.text.TextUtils;

import com.github.junrar.rarfile.FileHeader;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipEncoding;

import java.io.IOException;
import java.util.Date;

import androidx.annotation.NonNull;

public class RarArchiveEntry implements ArchiveEntry {

    @NonNull
    private final FileHeader mHeader;
    @NonNull
    private final String mName;

    public RarArchiveEntry(@NonNull FileHeader header, @NonNull ZipEncoding zipEncoding)
            throws IOException {
        mHeader = header;
        String name = mHeader.getFileNameW();
        if (TextUtils.isEmpty(name)) {
            name = zipEncoding.decode(mHeader.getFileNameByteArray());
        }
        name = name.replace('\\', '/');
        mName = name;
    }

    @NonNull
    FileHeader getHeader() {
        return mHeader;
    }

    @NonNull
    @Override
    public String getName() {
        return mName;
    }

    @Override
    public long getSize() {
        return mHeader.getFullUnpackSize();
    }

    @Override
    public boolean isDirectory() {
        return mHeader.isDirectory();
    }

    @NonNull
    @Override
    public Date getLastModifiedDate() {
        return mHeader.getMTime();
    }
}
