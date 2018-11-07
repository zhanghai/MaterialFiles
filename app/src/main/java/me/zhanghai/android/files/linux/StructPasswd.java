/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.linux;

import androidx.annotation.Nullable;

public final class StructPasswd {

    @Nullable
    public final String pw_name;
    public final int pw_uid;
    public final int pw_gid;
    @Nullable
    public final String pw_gecos;
    @Nullable
    public final String pw_dir;
    @Nullable
    public final String pw_shell;

    public StructPasswd(@Nullable String pw_name, int pw_uid, int pw_gid, @Nullable String pw_gecos,
                        @Nullable String pw_dir, @Nullable String pw_shell) {
        this.pw_name = pw_name;
        this.pw_uid = pw_uid;
        this.pw_gid = pw_gid;
        this.pw_gecos = pw_gecos;
        this.pw_dir = pw_dir;
        this.pw_shell = pw_shell;
    }
}
