/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import android.os.Parcel;

import org.apache.commons.compress.archivers.ArchiveEntry;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import me.zhanghai.android.files.provider.common.ParcelablePosixFileAttributes;

public class ArchiveFileAttributes extends ParcelablePosixFileAttributes {

    @NonNull
    private final String mEntryName;

    ArchiveFileAttributes(@NonNull Path archiveFile, @NonNull ArchiveEntry entry) {
        this(new ArchiveFileAttributesImpl(archiveFile, entry));
    }

    private ArchiveFileAttributes(@NonNull ArchiveFileAttributesImpl attributes) {
        super(attributes);

        mEntryName = attributes.getEntryName();
    }

    @NonNull
    public String getEntryName() {
        return mEntryName;
    }


    public static final Creator<ArchiveFileAttributes> CREATOR =
            new Creator<ArchiveFileAttributes>() {
                @Override
                public ArchiveFileAttributes createFromParcel(Parcel source) {
                    return new ArchiveFileAttributes(source);
                }
                @Override
                public ArchiveFileAttributes[] newArray(int size) {
                    return new ArchiveFileAttributes[size];
                }
            };

    protected ArchiveFileAttributes(Parcel in) {
        super(in);

        mEntryName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeString(mEntryName);
    }
}
