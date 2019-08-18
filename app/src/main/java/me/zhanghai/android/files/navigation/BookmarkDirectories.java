/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.settings.Settings;
import me.zhanghai.java.functional.Functional;

public class BookmarkDirectories {

    private BookmarkDirectories() {}

    public static void add(@NonNull BookmarkDirectory bookmarkDirectory) {
        List<BookmarkDirectory> bookmarkDirectories = Settings.BOOKMARK_DIRECTORIES.getValue();
        bookmarkDirectories = new ArrayList<>(bookmarkDirectories);
        bookmarkDirectories.add(bookmarkDirectory);
        Settings.BOOKMARK_DIRECTORIES.putValue(bookmarkDirectories);
    }

    public static void move(int fromPosition, int toPosition) {
        List<BookmarkDirectory> bookmarkDirectories = Settings.BOOKMARK_DIRECTORIES.getValue();
        bookmarkDirectories = new ArrayList<>(bookmarkDirectories);
        BookmarkDirectory bookmarkDirectory = bookmarkDirectories.remove(fromPosition);
        bookmarkDirectories.add(toPosition, bookmarkDirectory);
        Settings.BOOKMARK_DIRECTORIES.putValue(bookmarkDirectories);
    }

    public static void replace(@NonNull BookmarkDirectory bookmarkDirectory) {
        List<BookmarkDirectory> bookmarkDirectories = Settings.BOOKMARK_DIRECTORIES.getValue();
        bookmarkDirectories = new ArrayList<>(bookmarkDirectories);
        int index = Functional.findIndex(bookmarkDirectories, bookmarkDirectory2 ->
                bookmarkDirectory2.getId() == bookmarkDirectory.getId());
        bookmarkDirectories.set(index, bookmarkDirectory);
        Settings.BOOKMARK_DIRECTORIES.putValue(bookmarkDirectories);
    }

    public static void remove(@NonNull BookmarkDirectory bookmarkDirectory) {
        List<BookmarkDirectory> bookmarkDirectories = Settings.BOOKMARK_DIRECTORIES.getValue();
        bookmarkDirectories = new ArrayList<>(bookmarkDirectories);
        int index = Functional.findIndex(bookmarkDirectories, bookmarkDirectory2 ->
                bookmarkDirectory2.getId() == bookmarkDirectory.getId());
        bookmarkDirectories.remove(index);
        Settings.BOOKMARK_DIRECTORIES.putValue(bookmarkDirectories);
    }
}
