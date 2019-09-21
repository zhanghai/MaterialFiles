/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.syscall;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.provider.common.ByteString;

public final class StructMntent implements Parcelable {

    @NonNull
    public final ByteString mnt_fsname;
    @NonNull
    public final ByteString mnt_dir;
    @NonNull
    public final ByteString mnt_type;
    @NonNull
    public final ByteString mnt_opts;
    public final int mnt_freq;
    public final int mnt_passno;

    public StructMntent(@NonNull ByteString mnt_fsname, @NonNull ByteString mnt_dir,
                        @NonNull ByteString mnt_type, @NonNull ByteString mnt_opts, int mnt_freq,
                        int mnt_passno) {
        this.mnt_fsname = mnt_fsname;
        this.mnt_dir = mnt_dir;
        this.mnt_type = mnt_type;
        this.mnt_opts = mnt_opts;
        this.mnt_freq = mnt_freq;
        this.mnt_passno = mnt_passno;
    }


    public static final Creator<StructMntent> CREATOR = new Creator<StructMntent>() {
        @Override
        public StructMntent createFromParcel(Parcel source) {
            return new StructMntent(source);
        }
        @Override
        public StructMntent[] newArray(int size) {
            return new StructMntent[size];
        }
    };

    protected StructMntent(Parcel in) {
        mnt_fsname = in.readParcelable(ByteString.class.getClassLoader());
        mnt_dir = in.readParcelable(ByteString.class.getClassLoader());
        mnt_type = in.readParcelable(ByteString.class.getClassLoader());
        mnt_opts = in.readParcelable(ByteString.class.getClassLoader());
        mnt_freq = in.readInt();
        mnt_passno = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mnt_fsname, flags);
        dest.writeParcelable(mnt_dir, flags);
        dest.writeParcelable(mnt_type, flags);
        dest.writeParcelable(mnt_opts, flags);
        dest.writeInt(mnt_freq);
        dest.writeInt(mnt_passno);
    }
}
