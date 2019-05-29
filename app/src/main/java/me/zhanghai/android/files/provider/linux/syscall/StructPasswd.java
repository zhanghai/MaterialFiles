/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.syscall;

import androidx.annotation.Nullable;
import me.zhanghai.android.files.provider.common.ByteString;

public final class StructPasswd {

    @Nullable
    public final ByteString pw_name;
    public final int pw_uid;
    public final int pw_gid;
    @Nullable
    public final ByteString pw_gecos;
    @Nullable
    public final ByteString pw_dir;
    @Nullable
    public final ByteString pw_shell;

    public StructPasswd(@Nullable ByteString pw_name, int pw_uid, int pw_gid,
                        @Nullable ByteString pw_gecos, @Nullable ByteString pw_dir,
                        @Nullable ByteString pw_shell) {
        this.pw_name = pw_name;
        this.pw_uid = pw_uid;
        this.pw_gid = pw_gid;
        this.pw_gecos = pw_gecos;
        this.pw_dir = pw_dir;
        this.pw_shell = pw_shell;
    }
}
