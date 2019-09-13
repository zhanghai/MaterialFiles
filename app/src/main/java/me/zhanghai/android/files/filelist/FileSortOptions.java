/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import java9.util.Comparators;
import java9.util.function.Predicate;
import me.zhanghai.android.files.file.FileItem;
import me.zhanghai.android.files.util.NaturalOrderComparator;
import me.zhanghai.java.functional.Functional;
import me.zhanghai.java.functional.MoreComparator;

public class FileSortOptions implements Parcelable {

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
        Comparator<FileItem> comparator = Comparators.comparing(FileUtils::getName,
                Comparators.thenComparing(namePrefixComparator, new NaturalOrderComparator()));
        switch (mBy) {
            case NAME:
                // Nothing to do.
                break;
            case TYPE:
                comparator = Comparators.thenComparing(Comparators.comparing(
                        FileUtils::getExtension, String::compareToIgnoreCase), comparator);
                break;
            case SIZE:
                comparator = Comparators.thenComparing(Comparators.comparing(file ->
                        file.getAttributes().size()), comparator);
                break;
            case LAST_MODIFIED:
                comparator = Comparators.thenComparing(Comparators.comparing(file ->
                        file.getAttributes().lastModifiedTime()), comparator);
                break;
            default:
                throw new IllegalStateException();
        }
        switch (mOrder) {
            case ASCENDING:
                break;
            case DESCENDING:
                comparator = Comparators.reversed(comparator);
                break;
            default:
                throw new IllegalStateException();
        }
        if (mDirectoriesFirst) {
            Comparator<FileItem> isDirectoryComparator = Comparators.reversed(
                    MoreComparator.comparingBoolean(file -> file.getAttributes().isDirectory()));
            comparator = Comparators.thenComparing(isDirectoryComparator, comparator);
        }
        return comparator;
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        FileSortOptions that = (FileSortOptions) object;
        return mDirectoriesFirst == that.mDirectoriesFirst
                && mBy == that.mBy
                && mOrder == that.mOrder;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mBy, mOrder, mDirectoriesFirst);
    }

    public static final Creator<FileSortOptions> CREATOR = new Creator<FileSortOptions>() {
        @Override
        public FileSortOptions createFromParcel(Parcel source) {
            return new FileSortOptions(source);
        }
        @Override
        public FileSortOptions[] newArray(int size) {
            return new FileSortOptions[size];
        }
    };

    protected FileSortOptions(Parcel in) {
        int tmpMBy = in.readInt();
        mBy = tmpMBy == -1 ? null : By.values()[tmpMBy];
        int tmpMOrder = in.readInt();
        mOrder = tmpMOrder == -1 ? null : Order.values()[tmpMOrder];
        mDirectoriesFirst = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mBy == null ? -1 : mBy.ordinal());
        dest.writeInt(mOrder == null ? -1 : mOrder.ordinal());
        dest.writeByte(mDirectoriesFirst ? (byte) 1 : (byte) 0);
    }
}
