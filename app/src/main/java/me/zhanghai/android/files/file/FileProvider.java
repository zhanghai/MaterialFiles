/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.BuildConfig;

public class FileProvider extends ContentProvider {

    private static final String[] COLUMNS = {
            OpenableColumns.DISPLAY_NAME,
            OpenableColumns.SIZE,
            MediaStore.MediaColumns.DATA
    };

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public void attachInfo(@NonNull Context context, @NonNull ProviderInfo info) {
        super.attachInfo(context, info);

        if (info.exported) {
            throw new SecurityException("Provider must not be exported");
        }
        if (!info.grantUriPermissions) {
            throw new SecurityException("Provider must grant uri permissions");
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // ContentProvider has already checked granted permissions
        File file = getFileForUri(uri);
        if (projection == null) {
            projection = COLUMNS;
        }
        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        for (String column : projection) {
            if (TextUtils.isEmpty(column)) {
                continue;
            }
            switch (column) {
                case OpenableColumns.DISPLAY_NAME:
                    columns.add(column);
                    values.add(file.getName());
                    break;
                case OpenableColumns.SIZE:
                    columns.add(column);
                    values.add(file.length());
                    break;
                case MediaStore.MediaColumns.DATA:
                    columns.add(column);
                    values.add(file.getAbsolutePath());
                    break;
            }
        }
        MatrixCursor cursor = new MatrixCursor(columns.toArray(new String[columns.size()]), 1);
        cursor.addRow(values);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        File file = getFileForUri(uri);
        return MimeTypes.getMimeType(file.getPath());
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("No external inserts");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("No external updates");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        // ContentProvider has already checked granted permissions
        File file = getFileForUri(uri);
        // TODO: Directory?
        return file.delete() ? 1 : 0;
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode)
            throws FileNotFoundException {
        // ContentProvider has already checked granted permissions
        File file = getFileForUri(uri);
        int modeBits = ParcelFileDescriptor.parseMode(mode);
        return ParcelFileDescriptor.open(file, modeBits);
    }

    @Nullable
    public static Uri getUriForPath(@NonNull String path) {
        return new Uri.Builder()
                .scheme("content")
                .authority(BuildConfig.FILE_PROVIDIER_AUTHORITY)
                .path(path)
                .build();
    }

    @NonNull
    public static String getPathForUri(@NonNull Uri uri) {
        return uri.getPath();
    }

    @NonNull
    private static File getFileForUri(@NonNull Uri uri) {
        return new File(getPathForUri(uri));
    }
}
