/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.functional.ComparatorCompat;

public class FileSortOptions {

    public enum By {
        NAME,
        TYPE,
        SIZE,
        MODIFIED
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

    public Comparator<File> makeComparator() {
        Comparator<File> comparator;
        switch (mBy) {
            case NAME:
                comparator = ComparatorCompat.comparing(File::getName, String::compareToIgnoreCase);
                break;
            case TYPE:
                comparator = ComparatorCompat.thenComparing(
                        ComparatorCompat.comparing(File::getExtension, String::compareToIgnoreCase),
                        ComparatorCompat.comparing(File::getName, String::compareToIgnoreCase));
                break;
            case SIZE:
                comparator = ComparatorCompat.comparing(File::getSize);
                break;
            case MODIFIED:
                comparator = ComparatorCompat.comparing(File::getModified);
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

    public void sort(List<File> fileList) {
        Collections.sort(fileList, makeComparator());
    }
}
