/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import java.util.Comparator;

import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.functional.ComparatorCompat;

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
        // FIXME: Should sort with integer in mind.
        Comparator<File> comparator = ComparatorCompat.comparing(File::getName,
                String::compareToIgnoreCase);
        switch (mBy) {
            case NAME:
                // Nothing to do here.
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
            Comparator<File> isDirectoryComparator = ComparatorCompat.comparingInt(
                    file -> file.isDirectory() ? -1 : 1);
            comparator = ComparatorCompat.thenComparing(isDirectoryComparator, comparator);
        }
        return comparator;
    }
}
