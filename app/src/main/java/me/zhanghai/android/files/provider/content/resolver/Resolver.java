/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.content.resolver;

import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.AppProvider;
import me.zhanghai.android.files.file.MimeTypes;
import me.zhanghai.android.files.util.IoUtils;

public class Resolver {

    private Resolver() {}

    public static void checkExistence(@NonNull Uri uri) throws ResolverException {
        int rowCount;
        try (Cursor cursor = query(uri, new String[0], null, null, null)) {
            rowCount = cursor.getCount();
        }
        if (rowCount < 1) {
            throw new ResolverException(new FileNotFoundException("Row count is less than 1: "
                    + rowCount));
        }
    }

    public static void delete(@NonNull Uri uri) throws ResolverException {
        int deletedRowCount;
        try {
            deletedRowCount = getContentResolver().delete(uri, null, null);
        } catch (Exception e) {
            throw new ResolverException(e);
        }
        if (deletedRowCount < 1) {
            throw new ResolverException(new FileNotFoundException(
                    "Deleted row count is less than 1: " + deletedRowCount));
        }
    }

    public static boolean exists(@NonNull Uri uri) {
        try {
            checkExistence(uri);
            return true;
        } catch (ResolverException e) {
            return false;
        }
    }

    @Nullable
    public static String getDisplayName(@NonNull Uri uri) throws ResolverException {
        try (Cursor cursor = query(uri, new String[] { OpenableColumns.DISPLAY_NAME }, null, null,
                null)) {
            Cursors.moveToFirst(cursor);
            return Cursors.getString(cursor, OpenableColumns.DISPLAY_NAME);
        }
    }

    public static long getSize(@NonNull Uri uri) throws ResolverException {
        try (Cursor cursor = query(uri, new String[] { OpenableColumns.SIZE }, null, null, null)) {
            Cursors.moveToFirst(cursor);
            return Cursors.getLong(cursor, OpenableColumns.SIZE);
        }
    }

    @Nullable
    public static String getMimeType(@NonNull Uri uri) throws ResolverException {
        String mimeType;
        try {
            mimeType = getContentResolver().getType(uri);
        } catch (Exception e) {
            throw new ResolverException(e);
        }
        if (TextUtils.isEmpty(mimeType) || Objects.equals(mimeType, MimeTypes.GENERIC_MIME_TYPE)) {
            return null;
        }
        return mimeType;
    }

    @NonNull
    public static AssetFileDescriptor openAssetFileDescriptor(@NonNull Uri uri,
                                                              @NonNull String mode)
            throws ResolverException {
        AssetFileDescriptor descriptor;
        try {
            descriptor = getContentResolver().openAssetFileDescriptor(uri, mode);
        } catch (Exception e) {
            throw new ResolverException(e);
        }
        if (descriptor == null) {
            throw new ResolverException("ContentResolver.openAssetFileDescriptor() returned null: "
                    + uri);
        }
        return descriptor;
    }

    @NonNull
    public static InputStream openInputStream(@NonNull Uri uri, @NonNull String mode)
            throws ResolverException {
        AssetFileDescriptor descriptor = openAssetFileDescriptor(uri, mode);
        try {
            return descriptor.createInputStream();
        } catch (IOException e) {
            IoUtils.close(descriptor);
            throw new ResolverException(e);
        }
    }

    @NonNull
    public static OutputStream openOutputStream(@NonNull Uri uri, @NonNull String mode)
            throws ResolverException {
        AssetFileDescriptor descriptor = openAssetFileDescriptor(uri, mode);
        try {
            return descriptor.createOutputStream();
        } catch (IOException e) {
            IoUtils.close(descriptor);
            throw new ResolverException(e);
        }
    }

    @NonNull
    public static ParcelFileDescriptor openParcelFileDescriptor(@NonNull Uri uri,
                                                                @NonNull String mode)
            throws ResolverException {
        ParcelFileDescriptor descriptor;
        try {
            descriptor = getContentResolver().openFileDescriptor(uri, mode);
        } catch (Exception e) {
            throw new ResolverException(e);
        }
        if (descriptor == null) {
            throw new ResolverException("ContentResolver.openFileDescriptor() returned null: "
                    + uri);
        }
        return descriptor;
    }

    @NonNull
    public static Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                               @Nullable String selection, @Nullable String[] selectionArgs,
                               @Nullable String sortOrder) throws ResolverException {
        Cursor cursor;
        try {
            cursor = getContentResolver().query(uri, projection, selection, selectionArgs,
                    sortOrder);
        } catch (Exception e) {
            throw new ResolverException(e);
        }
        if (cursor == null) {
            throw new ResolverException("ContentResolver.query() returned null: " + uri);
        }
        return cursor;
    }

    @NonNull
    public static ContentResolver getContentResolver() {
        return AppProvider.requireContext().getContentResolver();
    }
}
