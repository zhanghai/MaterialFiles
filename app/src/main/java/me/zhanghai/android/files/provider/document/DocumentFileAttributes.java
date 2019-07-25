/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.document;

import android.net.Uri;
import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.provider.common.ParcelableContentProviderFileAttributes;

public class DocumentFileAttributes extends ParcelableContentProviderFileAttributes {

    private final int mFlags;

    DocumentFileAttributes(long lastModifiedTimeMillis, @Nullable String mimeType, long size,
                           int flags, @NonNull Uri uri) {
        this(new DocumentFileAttributesImpl(lastModifiedTimeMillis, mimeType, size, flags, uri));
    }

    private DocumentFileAttributes(@NonNull DocumentFileAttributesImpl attributes) {
        super(attributes);

        mFlags = attributes.getFlags();
    }

    public int getFlags() {
        return mFlags;
    }


    public static final Creator<DocumentFileAttributes> CREATOR =
            new Creator<DocumentFileAttributes>() {
                @Override
                public DocumentFileAttributes createFromParcel(Parcel source) {
                    return new DocumentFileAttributes(source);
                }
                @Override
                public DocumentFileAttributes[] newArray(int size) {
                    return new DocumentFileAttributes[size];
                }
            };

    protected DocumentFileAttributes(Parcel in) {
        super(in);

        mFlags = in.readInt();
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeInt(mFlags);
    }
}
