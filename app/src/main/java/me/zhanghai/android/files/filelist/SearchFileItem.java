/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.os.Parcel;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import me.zhanghai.android.files.file.FileItem;

public class SearchFileItem extends FileItem implements Comparable<SearchFileItem> {

    private static final AtomicLong sId = new AtomicLong();

    private final long mId;

    private SearchFileItem(@NonNull FileItem fileItem) {
        super(fileItem);

        mId = sId.getAndIncrement();
    }

    @NonNull
    public static SearchFileItem load(@NonNull Path path) throws IOException {
        return new SearchFileItem(FileItem.load(path));
    }

    @Override
    public int compareTo(@NonNull SearchFileItem other) {
        return Long.compare(mId, other.mId);
    }


    public static final Creator<SearchFileItem> CREATOR = new Creator<SearchFileItem>() {
        @Override
        public SearchFileItem createFromParcel(Parcel source) {
            return new SearchFileItem(source);
        }
        @Override
        public SearchFileItem[] newArray(int size) {
            return new SearchFileItem[size];
        }
    };

    protected SearchFileItem(Parcel in) {
        super(in);

        mId = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeLong(mId);
    }
}
