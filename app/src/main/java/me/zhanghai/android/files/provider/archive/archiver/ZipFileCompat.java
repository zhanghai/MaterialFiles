/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive.archiver;

import android.os.Build;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ZipFileCompat implements Closeable {

    @NonNull
    private final ZipFile mZipFile;
    @NonNull
    private final java.util.zip.ZipFile mJavaZipFile;

    public ZipFileCompat(@NonNull File file, @NonNull String encoding) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mZipFile = new ZipFile(file, encoding);
            mJavaZipFile = null;
        } else {
            mZipFile = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // The same charset logic as Apache Commons Compress.
                Charset charset;
                try {
                    charset = Charset.forName(encoding);
                } catch (UnsupportedCharsetException e) {
                    charset = Charset.defaultCharset();
                }
                mJavaZipFile = new java.util.zip.ZipFile(file, charset);
            } else {
                mJavaZipFile = new java.util.zip.ZipFile(file);
            }
        }
    }

    @NonNull
    public Enumeration<ZipArchiveEntry> getEntries() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return mZipFile.getEntries();
        } else {
            Enumeration<? extends ZipEntry> entries = mJavaZipFile.entries();
            return new Enumeration<ZipArchiveEntry>() {
                @Override
                public boolean hasMoreElements() {
                    return entries.hasMoreElements();
                }
                @NonNull
                @Override
                public ZipArchiveEntry nextElement() {
                    ZipEntry entry = entries.nextElement();
                    try {
                        return new ZipArchiveEntry(entry);
                    } catch (ZipException e) {
                        e.printStackTrace();
                        return new UnparseableExtraZipArchiveEntry(entry);
                    }
                }
            };
        }
    }

    @Nullable
    public InputStream getInputStream(@NonNull ZipArchiveEntry entry) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return mZipFile.getInputStream(entry);
        } else {
            return mJavaZipFile.getInputStream(entry);
        }
    }

    public void close() throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mZipFile.close();
        } else {
            mJavaZipFile.close();
        }
    }

    private static class UnparseableExtraZipArchiveEntry extends ZipArchiveEntry {

        public UnparseableExtraZipArchiveEntry(@NonNull ZipEntry entry) {
            super(entry.getName());

            setTime(entry.getTime());
            setExtra();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setLastModifiedTime(entry.getLastModifiedTime());
                setLastAccessTime(entry.getLastAccessTime());
                setCreationTime(entry.getCreationTime());
            }
            long crc = entry.getCrc();
            if (crc >= 0 && crc <= 0xFFFFFFFFL) {
                setCrc(entry.getCrc());
            }
            long size = entry.getSize();
            if (size >= 0) {
                setSize(size);
            }
            setCompressedSize(entry.getCompressedSize());
            setMethod(entry.getMethod());
            setComment(entry.getComment());
        }
    }
}
