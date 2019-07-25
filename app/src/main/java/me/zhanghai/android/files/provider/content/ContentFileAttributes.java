/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.content;

import android.net.Uri;
import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.provider.common.ParcelableContentProviderFileAttributes;

public class ContentFileAttributes extends ParcelableContentProviderFileAttributes {

    ContentFileAttributes(@Nullable String mimeType, long size, @NonNull Uri uri) {
        super(new ContentFileAttributesImpl(mimeType, size, uri));
    }


    public static final Creator<ContentFileAttributes> CREATOR =
            new Creator<ContentFileAttributes>() {
                @Override
                public ContentFileAttributes createFromParcel(Parcel source) {
                    return new ContentFileAttributes(source);
                }
                @Override
                public ContentFileAttributes[] newArray(int size) {
                    return new ContentFileAttributes[size];
                }
            };

    protected ContentFileAttributes(Parcel in) {
        super(in);
    }
}
