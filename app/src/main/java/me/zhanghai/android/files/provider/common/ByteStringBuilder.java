/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.util.Arrays;

import androidx.annotation.NonNull;

public class ByteStringBuilder {

    @NonNull
    private byte[] mBytes;
    private int mLength;

    public ByteStringBuilder() {
        this(16);
    }

    public ByteStringBuilder(int capacity) {
        mBytes = new byte[capacity];
    }

    public byte byteAt(int index) {
        if (!(index >= 0 && index < mLength)) {
            throw new IndexOutOfBoundsException();
        }
        return mBytes[index];
    }

    public int capacity() {
        return mBytes.length;
    }

    public int length() {
        return mLength;
    }

    public boolean isEmpty() {
        return mLength == 0;
    }

    @NonNull
    public ByteStringBuilder append(byte b) {
        ensureCapacity(mLength + 1);
        mBytes[mLength] = b;
        ++mLength;
        return this;
    }

    @NonNull
    public ByteStringBuilder append(@NonNull ByteString byteString) {
        int byteStringLength = byteString.length();
        ensureCapacity(mLength + byteStringLength);
        System.arraycopy(byteString.getOwnedBytes(), 0, mBytes, mLength, byteStringLength);
        mLength += byteStringLength;
        return this;
    }

    private void ensureCapacity(int minimumCapacity) {
        int capacity = mBytes.length;
        if (minimumCapacity > capacity) {
            int newCapacity = (capacity << 1) + 2;
            mBytes = Arrays.copyOf(mBytes, newCapacity);
        }
    }

    @NonNull
    public ByteString toByteString() {
        return new ByteString(mBytes, 0, mLength);
    }

    @NonNull
    @Override
    public String toString() {
        return new String(mBytes, 0, mLength);
    }
}
