/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.util.Objects;

import java8.nio.file.AccessMode;

public class AccessModes {

    private final boolean mRead;
    private final boolean mWrite;
    private final boolean mExecute;

    public AccessModes(boolean read, boolean write, boolean execute) {
        mRead = read;
        mWrite = write;
        mExecute = execute;
    }

    public boolean hasRead() {
        return mRead;
    }

    public boolean hasWrite() {
        return mWrite;
    }

    public boolean hasExecute() {
        return mExecute;
    }

    public static AccessModes fromArray(AccessMode[] modes) {
        boolean read = false;
        boolean write = false;
        boolean execute = false;
        for (AccessMode mode : modes) {
            Objects.requireNonNull(mode);
            switch (mode) {
                case READ:
                    read = true;
                    break;
                case WRITE:
                    write = true;
                    break;
                case EXECUTE:
                    execute = true;
                    break;
                default:
                    throw new UnsupportedOperationException(mode.toString());
            }
        }
        return new AccessModes(read, write, execute);
    }
}
