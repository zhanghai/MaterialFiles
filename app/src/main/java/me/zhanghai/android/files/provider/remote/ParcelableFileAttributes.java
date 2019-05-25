/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.Nullable;
import java8.nio.file.attribute.FileAttribute;
import me.zhanghai.android.files.provider.common.PosixFileMode;
import me.zhanghai.android.files.provider.common.PosixFileModeBit;

public class ParcelableFileAttributes implements Parcelable {

    private static final String POSIX_FILE_MODE_ATTRIBUTE_NAME = PosixFileMode.toAttribute(
            Collections.emptySet()).name();

    @Nullable
    private FileAttribute<?>[] mFileAttributes;

    public ParcelableFileAttributes(@Nullable FileAttribute<?>[] fileAttributes) {
        mFileAttributes = fileAttributes;
    }

    @Nullable
    public FileAttribute<?>[] get() {
        return mFileAttributes;
    }


    public static final Creator<ParcelableFileAttributes> CREATOR =
            new Creator<ParcelableFileAttributes>() {
                @Override
                public ParcelableFileAttributes createFromParcel(Parcel source) {
                    return new ParcelableFileAttributes(source);
                }
                @Override
                public ParcelableFileAttributes[] newArray(int size) {
                    return new ParcelableFileAttributes[size];
                }
            };

    protected ParcelableFileAttributes(Parcel in) {
        //noinspection unchecked
        EnumSet<PosixFileModeBit>[] modes = (EnumSet<PosixFileModeBit>[]) in.readSerializable();
        if (modes == null) {
            return;
        }
        mFileAttributes = new FileAttribute<?>[modes.length];
        for (int i = 0; i < modes.length; ++i) {
            EnumSet<PosixFileModeBit> mode = modes[i];
            if (mode == null) {
                continue;
            }
            mFileAttributes[i] = PosixFileMode.toAttribute(mode);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (mFileAttributes == null) {
            dest.writeSerializable(null);
            return;
        }
        //noinspection unchecked
        EnumSet<PosixFileModeBit>[] modes = (EnumSet<PosixFileModeBit>[]) new EnumSet[
                mFileAttributes.length];
        for (int i = 0; i < mFileAttributes.length; ++i) {
            FileAttribute<?> attribute = mFileAttributes[i];
            if (attribute == null) {
                continue;
            }
            if (!Objects.equals(attribute.name(), POSIX_FILE_MODE_ATTRIBUTE_NAME)) {
                throw new UnsupportedOperationException(attribute.name());
            }
            //noinspection unchecked
            FileAttribute<Set<PosixFileModeBit>> modeAttribute =
                    (FileAttribute<Set<PosixFileModeBit>>) attribute;
            modes[i] = EnumSet.copyOf(modeAttribute.value());
        }
        dest.writeSerializable(modes);
    }
}
