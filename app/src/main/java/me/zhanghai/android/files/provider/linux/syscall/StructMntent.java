/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.syscall;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.provider.common.ByteString;

public final class StructMntent {

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
}
