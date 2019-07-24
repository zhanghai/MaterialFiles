/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.document;

import android.net.Uri;
import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.provider.common.ParcelableBasicFileAttributes;

public class DocumentFileAttributes extends ParcelableBasicFileAttributes {

    DocumentFileAttributes(@Nullable String mimeType, long size, long lastModifiedTimeMillis,
                           @NonNull Uri uri) {
        super(new DocumentFileAttributesImpl(mimeType, size, lastModifiedTimeMillis, uri));
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
    }
}
