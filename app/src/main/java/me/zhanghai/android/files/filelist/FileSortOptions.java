/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import java9.util.function.Predicate;
import me.zhanghai.android.files.functional.ComparatorCompat;
import me.zhanghai.android.files.functional.Functional;
import me.zhanghai.android.files.functional.MoreComparator;
import me.zhanghai.android.files.util.NaturalOrderComparator;

public class FileSortOptions {

    public enum By {
        NAME,
        TYPE,
        SIZE,
        LAST_MODIFIED
    }

    public enum Order {
        ASCENDING,
        DESCENDING
    }

    // Same behavior as Nautilus.
    private static final List<String> NAME_UNIMPORTANT_PREFIXES = Arrays.asList(".", "#");

    @NonNull
    private final By mBy;
    @NonNull
    private final Order mOrder;
    @NonNull
    private final boolean mDirectoriesFirst;

    public FileSortOptions(@NonNull By by, @NonNull Order order, boolean directoriesFirst) {
        mBy = by;
        mOrder = order;
        mDirectoriesFirst = directoriesFirst;
    }

    @NonNull
    public By getBy() {
        return mBy;
    }

    @NonNull
    public Order getOrder() {
        return mOrder;
    }

    public boolean isDirectoriesFirst() {
        return mDirectoriesFirst;
    }

    @NonNull
    public FileSortOptions withBy(@NonNull FileSortOptions.By by) {
        return new FileSortOptions(by, mOrder, mDirectoriesFirst);
    }

    @NonNull
    public FileSortOptions withOrder(@NonNull FileSortOptions.Order order) {
        return new FileSortOptions(mBy, order, mDirectoriesFirst);
    }

    @NonNull
    public FileSortOptions withDirectoriesFirst(boolean directoriesFirst) {
        return new FileSortOptions(mBy, mOrder, directoriesFirst);
    }

    @NonNull
    public Comparator<FileItem> makeComparator() {
        Comparator<String> namePrefixComparator = MoreComparator.comparingBoolean(name ->
                Functional.some(NAME_UNIMPORTANT_PREFIXES, (Predicate<String>) name::startsWith));
        Comparator<FileItem> comparator = ComparatorCompat.comparing(FileUtils::getName,
                ComparatorCompat.thenComparing(namePrefixComparator, new NaturalOrderComparator()));
        switch (mBy) {
            case NAME:
                // Nothing to do.
                break;
            case TYPE:
                comparator = ComparatorCompat.thenComparing(ComparatorCompat.comparing(
                        FileUtils::getExtension, String::compareToIgnoreCase), comparator);
                break;
            case SIZE:
                comparator = ComparatorCompat.thenComparing(ComparatorCompat.comparing(file ->
                        file.getAttributes().size()), comparator);
                break;
            case LAST_MODIFIED:
                comparator = ComparatorCompat.thenComparing(ComparatorCompat.comparing(file ->
                        file.getAttributes().lastModifiedTime()), comparator);
                break;
            default:
                throw new IllegalStateException();
        }
        switch (mOrder) {
            case ASCENDING:
                break;
            case DESCENDING:
                comparator = ComparatorCompat.reversed(comparator);
                break;
            default:
                throw new IllegalStateException();
        }
        if (mDirectoriesFirst) {
            Comparator<FileItem> isDirectoryComparator = ComparatorCompat.reversed(
                    MoreComparator.comparingBoolean(file -> file.getAttributes().isDirectory()));
            comparator = ComparatorCompat.thenComparing(isDirectoryComparator, comparator);
        }
        return comparator;
    }
}
