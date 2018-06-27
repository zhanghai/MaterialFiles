/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.file;

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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.materialfilemanager.BuildConfig;

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

    @Override
    public String getType(@NonNull Uri uri) {
        File file = getFileForUri(uri);
        return MimeTypes.getMimeType(file.getPath());
    }

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

    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode)
            throws FileNotFoundException {
        // ContentProvider has already checked granted permissions
        File file = getFileForUri(uri);
        int modeBits = ParcelFileDescriptor.parseMode(mode);
        return ParcelFileDescriptor.open(file, modeBits);
    }

    public static Uri getUriForFile(File file) {
        String path = file.getAbsolutePath();
        return new Uri.Builder()
                .scheme("content")
                .authority(BuildConfig.FILE_PROVIDIER_AUTHORITY)
                .path(path)
                .build();
    }

    public static File getFileForUri(Uri uri) {
        String path = uri.getPath();
        return new File(path);
    }
}
