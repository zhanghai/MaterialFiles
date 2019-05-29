/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.syscall;

import androidx.annotation.Nullable;
import me.zhanghai.android.files.provider.common.ByteString;

public final class StructGroup {

    @Nullable
    public final ByteString gr_name;
    @Nullable
    public final ByteString gr_passwd;
    public final int gr_gid;
    @Nullable
    public final ByteString[] gr_mem;

    public StructGroup(@Nullable ByteString gr_name, @Nullable ByteString gr_passwd, int gr_gid,
                       @Nullable ByteString[] gr_mem) {
        this.gr_name = gr_name;
        this.gr_passwd = gr_passwd;
        this.gr_gid = gr_gid;
        this.gr_mem = gr_mem;
    }
}
