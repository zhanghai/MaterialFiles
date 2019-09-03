/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive.archiver;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

import org.apache.commons.compress.archivers.zip.ZipEncoding;
import org.apache.commons.compress.archivers.zip.ZipEncodingHelper;
import org.apache.commons.compress.utils.IOUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RarFile implements Closeable {

    public static final String RAR = "rar";

    private static final byte[] SIGNATURE_OLD = { 0x52, 0x45, 0x7e, 0x5e };
    private static final byte[] SIGNATURE_V4 = { 0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x00 };
    private static final byte[] SIGNATURE_V5 = { 0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x01 };

    @NonNull
    private final Archive mArchive;
    @NonNull
    private final ZipEncoding mZipEncoding;

    @Nullable
    public static String detect(@NonNull InputStream inputStream) throws IOException {
        if (!inputStream.markSupported()) {
            throw new IllegalArgumentException("InputStream.markSupported() returned false");
        }
        byte[] signature = new byte[Math.max(SIGNATURE_OLD.length, SIGNATURE_V4.length)];
        inputStream.mark(signature.length);
        int signatureLength;
        try {
            signatureLength = IOUtils.readFully(inputStream, signature);
        } finally {
            inputStream.reset();
        }
        if (matches(signature, signatureLength)) {
            return RAR;
        }
        return null;
    }

    public static boolean matches(@NonNull byte[] signature, int length) {
        return matches(signature, length, SIGNATURE_OLD) || matches(signature, length, SIGNATURE_V4)
                || matches(signature, length, SIGNATURE_V5);
    }

    private static boolean matches(@NonNull byte[] actual, int actualLength,
                                   @NonNull byte[] expected) {
        if (actualLength < expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; ++i) {
            if (actual[i] != expected[i]) {
                return false;
            }
        }
        return true;
    }

    public RarFile(@NonNull File file, @Nullable String encoding) throws IOException {
        try {
            mArchive = new Archive(file, null);
        } catch (RarException e) {
            throw new ArchiveException(e);
        }
        mZipEncoding = ZipEncodingHelper.getZipEncoding(encoding);
    }

    @Nullable
    public RarArchiveEntry getNextEntry() throws IOException {
        FileHeader header = mArchive.nextFileHeader();
        return header != null ? new RarArchiveEntry(header, mZipEncoding) : null;
    }

    @NonNull
    public Iterable<RarArchiveEntry> getEntries() throws IOException {
        List<RarArchiveEntry> entries = new ArrayList<>();
        for (FileHeader header : mArchive.getFileHeaders()) {
            entries.add(new RarArchiveEntry(header, mZipEncoding));
        }
        return entries;
    }

    @NonNull
    public InputStream getInputStream(@NonNull RarArchiveEntry entry) throws IOException {
        PipedInputStream inputStream = new PipedInputStream();
        PipedOutputStream outputStream = new PipedOutputStream(inputStream);
        new Thread(() -> {
            try {
                mArchive.extractFile(entry.getHeader(), outputStream);
                outputStream.close();
            } catch (IOException | RarException e) {
                e.printStackTrace();
            }
        }).start();
        return inputStream;
    }

    @Override
    public void close() throws IOException {
        mArchive.close();
    }
}
