/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.document;

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.DocumentsContract;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import java8.nio.file.attribute.BasicFileAttributeView;
import java8.nio.file.attribute.FileTime;
import me.zhanghai.android.files.provider.content.resolver.Cursors;
import me.zhanghai.android.files.provider.content.resolver.Resolver;
import me.zhanghai.android.files.provider.content.resolver.ResolverException;
import me.zhanghai.android.files.provider.document.resolver.DocumentResolver;

public class DocumentFileAttributeView implements BasicFileAttributeView, Parcelable {

    private static final String NAME = DocumentFileSystemProvider.SCHEME;

    static final Set<String> SUPPORTED_NAMES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("basic", NAME)));

    @NonNull
    private final DocumentPath mPath;

    DocumentFileAttributeView(@NonNull DocumentPath path) {
        mPath = path;
    }

    @NonNull
    @Override
    public String name() {
        return NAME;
    }

    @NonNull
    @Override
    public DocumentFileAttributes readAttributes() throws IOException {
        Uri uri;
        try {
            uri = DocumentResolver.getDocumentUri(mPath);
        } catch (ResolverException e) {
            throw e.toFileSystemException(mPath.toString());
        }
        String mimeType;
        long size;
        long lastModifiedTimeMillis;
        try (Cursor cursor = Resolver.query(uri, new String[] {
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_SIZE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED
        }, null, null, null)) {
            mimeType = Cursors.getString(cursor, DocumentsContract.Document.COLUMN_MIME_TYPE);
            size = Cursors.getLong(cursor, DocumentsContract.Document.COLUMN_SIZE);
            lastModifiedTimeMillis = Cursors.getLong(cursor,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED);
        } catch (ResolverException e) {
            throw e.toFileSystemException(mPath.toString());
        }
        return new DocumentFileAttributes(mimeType, size, lastModifiedTimeMillis, uri);
    }

    @Override
    public void setTimes(@Nullable FileTime lastModifiedTime, @Nullable FileTime lastAccessTime,
                         @Nullable FileTime createTime) {
        throw new UnsupportedOperationException();
    }


    public static final Creator<DocumentFileAttributeView> CREATOR =
            new Creator<DocumentFileAttributeView>() {
                @Override
                public DocumentFileAttributeView createFromParcel(Parcel source) {
                    return new DocumentFileAttributeView(source);
                }
                @Override
                public DocumentFileAttributeView[] newArray(int size) {
                    return new DocumentFileAttributeView[size];
                }
            };

    protected DocumentFileAttributeView(Parcel in) {
        mPath = in.readParcelable(Path.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable((Parcelable) mPath, flags);
    }
}
