/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;
import java.util.Objects;

import androidx.annotation.NonNull;

public class ByteString implements Comparable<ByteString>, Parcelable {

    @NonNull
    private final byte[] mBytes;

    public ByteString(@NonNull byte[] bytes) {
        Objects.requireNonNull(bytes);
        mBytes = bytes.clone();
    }

    public ByteString(@NonNull byte[] bytes, int start, int end) {
        Objects.requireNonNull(bytes);
        mBytes = Arrays.copyOfRange(bytes, start, end);
    }

    @NonNull
    public byte[] getOwnedBytes() {
        return mBytes;
    }

    public byte byteAt(int index) {
        if (!(index >= 0 && index < mBytes.length)) {
            throw new IndexOutOfBoundsException();
        }
        return mBytes[index];
    }

    public int length() {
        return mBytes.length;
    }

    public boolean isEmpty() {
        return mBytes.length == 0;
    }

    public int indexOf(byte b) {
        return indexOf(b, 0);
    }

    public int indexOf(byte b, int fromIndex) {
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        for (int i = fromIndex; i < mBytes.length; ++i) {
            if (mBytes[i] == b) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(byte b) {
        return lastIndexOf(b, mBytes.length - 1);
    }

    public int lastIndexOf(byte b, int fromIndex) {
        if (fromIndex >= mBytes.length) {
            fromIndex = mBytes.length - 1;
        }
        for (int i = fromIndex; i >= 0; --i) {
            if (mBytes[i] == b) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    public ByteString substring(int start) {
        return substring(start, mBytes.length);
    }

    @NonNull
    public ByteString substring(int start, int end) {
        if (!(start >= 0 && start <= end && end <= mBytes.length)) {
            throw new IndexOutOfBoundsException();
        }
        if (start == 0 && end == mBytes.length) {
            return this;
        }
        return new ByteString(Arrays.copyOfRange(mBytes, start, end));
    }

    @NonNull
    @Override
    public String toString() {
        // This uses replacement char instead of throwing exception.
        return new String(mBytes);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ByteString byteString = (ByteString) object;
        return Arrays.equals(mBytes, byteString.mBytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(mBytes);
    }

    @Override
    public int compareTo(@NonNull ByteString other) {
        Objects.requireNonNull(other);
        return compareBytes(mBytes, other.mBytes);
    }

    private static int compareBytes(@NonNull byte[] bytes1, @NonNull byte[] bytes2) {
        int length1 = bytes1.length;
        int length2 = bytes2.length;
        int minLength = Math.min(length1, length2);
        for (int i = 0; i < minLength; ++i) {
            byte byte1 = bytes1[i];
            byte byte2 = bytes2[i];
            if (byte1 != byte2) {
                return byte1 - byte2;
            }
        }
        return length1 - length2;
    }


    public static final Creator<ByteString> CREATOR = new Creator<ByteString>() {
        @Override
        public ByteString createFromParcel(Parcel source) {
            return new ByteString(source);
        }
        @Override
        public ByteString[] newArray(int size) {
            return new ByteString[size];
        }
    };

    protected ByteString(Parcel in) {
        mBytes = in.createByteArray();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(mBytes);
    }
}
