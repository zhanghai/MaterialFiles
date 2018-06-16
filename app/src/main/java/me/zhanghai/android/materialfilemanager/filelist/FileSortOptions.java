/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.functional.ComparatorCompat;
import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.functional.MoreComparator;
import me.zhanghai.android.materialfilemanager.functional.compat.Predicate;

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

    private By mBy;
    private Order mOrder;
    private boolean mDirectoriesFirst;

    public FileSortOptions(By by, Order order, boolean directoriesFirst) {
        mBy = by;
        mOrder = order;
        mDirectoriesFirst = directoriesFirst;
    }

    public By getBy() {
        return mBy;
    }

    public Order getOrder() {
        return mOrder;
    }

    public boolean isDirectoriesFirst() {
        return mDirectoriesFirst;
    }

    public FileSortOptions withBy(FileSortOptions.By by) {
        return new FileSortOptions(by, mOrder, mDirectoriesFirst);
    }

    public FileSortOptions withOrder(FileSortOptions.Order order) {
        return new FileSortOptions(mBy, order, mDirectoriesFirst);
    }

    public FileSortOptions withDirectoriesFirst(boolean directoriesFirst) {
        return new FileSortOptions(mBy, mOrder, directoriesFirst);
    }

    public Comparator<File> makeComparator() {
        Comparator<String> namePrefixComparator = MoreComparator.comparingBoolean(name ->
                Functional.some(NAME_UNIMPORTANT_PREFIXES, (Predicate<String>) name::startsWith));
        Comparator<File> comparator = ComparatorCompat.comparing(File::getName,
                ComparatorCompat.thenComparing(namePrefixComparator, new NaturalOrderComparator()));
        switch (mBy) {
            case NAME:
                // Nothing to do.
                break;
            case TYPE:
                comparator = ComparatorCompat.thenComparing(
                        ComparatorCompat.comparing(File::getExtension, String::compareToIgnoreCase),
                        comparator);
                break;
            case SIZE:
                comparator = ComparatorCompat.thenComparing(
                        ComparatorCompat.comparing(File::getSize), comparator);
                break;
            case LAST_MODIFIED:
                comparator = ComparatorCompat.thenComparing(
                        ComparatorCompat.comparing(File::getLastModified), comparator);
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
            Comparator<File> isDirectoryComparator = ComparatorCompat.reversed(
                    MoreComparator.comparingBoolean(File::isDirectory));
            comparator = ComparatorCompat.thenComparing(isDirectoryComparator, comparator);
        }
        return comparator;
    }
}
