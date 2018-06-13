/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.os.Build;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

public class ZipFileCompat implements Closeable {

    private ZipFile mZipFile;
    private java.util.zip.ZipFile mJavaZipFile;

    public ZipFileCompat(File file, String encoding) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mZipFile = new ZipFile(file, encoding);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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

    public Iterator<ZipArchiveEntry> getEntries() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Enumeration<ZipArchiveEntry> entries = mZipFile.getEntries();
            return new Iterator<ZipArchiveEntry>() {
                @Override
                public boolean hasNext() {
                    return entries.hasMoreElements();
                }
                @Override
                public ZipArchiveEntry next() {
                    return entries.nextElement();
                }
            };
        } else {
            Enumeration<? extends ZipEntry> entries = mJavaZipFile.entries();
            return new Iterator<ZipArchiveEntry>() {
                @Override
                public boolean hasNext() {
                    return entries.hasMoreElements();
                }
                @Override
                public ZipArchiveEntry next() {
                    ZipEntry entry = entries.nextElement();
                    try {
                        return new ZipArchiveEntry(entry);
                    } catch (ZipException e) {
                        e.printStackTrace();
                        return new BadExtraZipArchiveEntry(entry);
                    }
                }
            };
        }
    }

    public void close() throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mZipFile.close();
        } else {
            mJavaZipFile.close();
        }
    }

    private static class BadExtraZipArchiveEntry extends ZipArchiveEntry {

        public BadExtraZipArchiveEntry(ZipEntry entry) {
            setName(entry.getName());
            setExtra();
            setMethod(entry.getMethod());
            setTime(entry.getTime());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setLastModifiedTime(entry.getLastModifiedTime());
                setLastAccessTime(entry.getLastAccessTime());
                setCreationTime(entry.getCreationTime());
            }
            long size = entry.getSize();
            if (size >= 0) {
                setSize(size);
            }
            setCompressedSize(entry.getCompressedSize());
            setCrc(entry.getCrc());
            setMethod(entry.getMethod());
            setComment(entry.getComment());
        }
    }
}
