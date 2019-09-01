/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive.archiver_sevenzipjbinding;

import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.SevenZipException;

import java.io.IOException;
import java.io.OutputStream;

import androidx.annotation.NonNull;

public class StreamOutStream implements ISequentialOutStream {

    @NonNull
    private final OutputStream mStream;

    public StreamOutStream(@NonNull OutputStream stream) {
        mStream = stream;
    }

    @Override
    public synchronized int write(@NonNull byte[] bytes) throws SevenZipException {
        try {
            mStream.write(bytes);
        } catch (IOException e) {
            throw new SevenZipException(e);
        }
        return bytes.length;
    }
}
