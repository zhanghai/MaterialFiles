/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.content.resolver;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Cursors {

    public static void moveToFirst(@NonNull Cursor cursor) throws ResolverException {
        if (!cursor.moveToFirst()) {
            throw new ResolverException("Cursor.moveToFirst() returned false");
        }
    }

    public static int getInt(@NonNull Cursor cursor, @NonNull String columnName)
            throws ResolverException {
        int columnIndex;
        try {
            columnIndex = cursor.getColumnIndexOrThrow(columnName);
        } catch (IllegalArgumentException e) {
            throw new ResolverException(e);
        }
        return cursor.getInt(columnIndex);
    }

    public static long getLong(@NonNull Cursor cursor, @NonNull String columnName)
            throws ResolverException {
        int columnIndex;
        try {
            columnIndex = cursor.getColumnIndexOrThrow(columnName);
        } catch (IllegalArgumentException e) {
            throw new ResolverException(e);
        }
        return cursor.getLong(columnIndex);
    }

    @Nullable
    public static String getString(@NonNull Cursor cursor, @NonNull String columnName)
            throws ResolverException {
        int columnIndex;
        try {
            columnIndex = cursor.getColumnIndexOrThrow(columnName);
        } catch (IllegalArgumentException e) {
            throw new ResolverException(e);
        }
        return cursor.getString(columnIndex);
    }

    @NonNull
    public static String requireString(@NonNull Cursor cursor, @NonNull String columnName)
            throws ResolverException {
        String string = getString(cursor, columnName);
        if (string == null) {
            throw new ResolverException("Cursor.getString() returned null: " + columnName);
        }
        return string;
    }
}
