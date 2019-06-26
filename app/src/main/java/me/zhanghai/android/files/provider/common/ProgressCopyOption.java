/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import java8.nio.file.CopyOption;
import java9.util.function.LongConsumer;
import me.zhanghai.android.files.util.BundleBuilder;
import me.zhanghai.android.files.util.RemoteCallback;

public class ProgressCopyOption implements CopyOption, Parcelable {

    private static final String KEY_PREFIX = ProgressCopyOption.class.getName() + '.';

    private static final String KEY_COPIED_SIZE = KEY_PREFIX + "COPIED_SIZE";

    @NonNull
    private final LongConsumer mListener;

    private final long mIntervalMillis;

    public ProgressCopyOption(@NonNull LongConsumer listener, long intervalMillis) {
        mListener = listener;
        mIntervalMillis = intervalMillis;
    }

    @NonNull
    public LongConsumer getListener() {
        return mListener;
    }

    public long getIntervalMillis() {
        return mIntervalMillis;
    }


    public static final Creator<ProgressCopyOption> CREATOR = new Creator<ProgressCopyOption>() {
        @Override
        public ProgressCopyOption createFromParcel(Parcel source) {
            return new ProgressCopyOption(source);
        }
        @Override
        public ProgressCopyOption[] newArray(int size) {
            return new ProgressCopyOption[size];
        }
    };

    protected ProgressCopyOption(Parcel in) {
        RemoteCallback remoteCallback = in.readParcelable(RemoteCallback.class.getClassLoader());
        mListener = copiedSize -> remoteCallback.sendResult(new BundleBuilder()
                .putLong(KEY_COPIED_SIZE, copiedSize)
                .build());
        mIntervalMillis = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(new RemoteCallback(result -> mListener.accept(result.getLong(
                KEY_COPIED_SIZE, 0))), flags);
        dest.writeLong(mIntervalMillis);
    }
}
