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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Files;
import java8.nio.file.Path;
import java8.nio.file.Paths;
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
        Path path = getPathForUri(uri);
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
                    values.add(path.getFileName().toString());
                    break;
                case OpenableColumns.SIZE: {
                    long size;
                    try {
                        size = Files.size(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                        size = 0;
                    }
                    columns.add(column);
                    values.add(size);
                    break;
                }
                case MediaStore.MediaColumns.DATA: {
                    File file;
                    try {
                        file = path.toFile();
                    } catch (UnsupportedOperationException e) {
                        continue;
                    }
                    String absolutePath = file.getAbsolutePath();
                    columns.add(column);
                    values.add(absolutePath);
                    break;
                }
            }
        }
        MatrixCursor cursor = new MatrixCursor(columns.toArray(new String[columns.size()]), 1);
        cursor.addRow(values);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        Path path = getPathForUri(uri);
        return MimeTypes.getMimeType(path.toString());
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
        throw new UnsupportedOperationException("No external deletes");
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode)
            throws FileNotFoundException {
        // ContentProvider has already checked granted permissions
        Path path = getPathForUri(uri);
        // May throw UnsupportedOperationException.
        File file = path.toFile();
        int modeBits = ParcelFileDescriptor.parseMode(mode);
        return ParcelFileDescriptor.open(file, modeBits);
    }

    @Nullable
    public static Uri getUriForPath(@NonNull Path path) {
        return new Uri.Builder()
                .scheme("content")
                .authority(BuildConfig.FILE_PROVIDIER_AUTHORITY)
                .opaquePart(path.toUri().toString())
                .build();
    }

    @NonNull
    public static Path getPathForUri(@NonNull Uri uri) {
        URI pathUri;
        try {
            pathUri = new URI(uri.getSchemeSpecificPart());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        return Paths.get(pathUri);
    }
}
