/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.jni;

import androidx.annotation.Nullable;

public final class StructGroup {

    @Nullable
    public final String gr_name;
    @Nullable
    public final String gr_passwd;
    public final int gr_gid;
    @Nullable
    public final String[] gr_mem;

    public StructGroup(@Nullable String gr_name, @Nullable String gr_passwd, int gr_gid,
                       @Nullable String[] gr_mem) {
        this.gr_name = gr_name;
        this.gr_passwd = gr_passwd;
        this.gr_gid = gr_gid;
        this.gr_mem = gr_mem;
    }
}
