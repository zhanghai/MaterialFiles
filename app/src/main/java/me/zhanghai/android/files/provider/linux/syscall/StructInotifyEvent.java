/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.syscall;

import androidx.annotation.Nullable;
import me.zhanghai.android.files.provider.common.ByteString;

public final class StructInotifyEvent {

    public final int wd;
    public final int mask; /* uint32_t */
    public final int cookie; /* uint32_t */
    @Nullable
    public final ByteString name;

    public StructInotifyEvent(int wd, int mask, int cookie, @Nullable ByteString name) {
        this.wd = wd;
        this.mask = mask;
        this.cookie = cookie;
        this.name = name;
    }
}
