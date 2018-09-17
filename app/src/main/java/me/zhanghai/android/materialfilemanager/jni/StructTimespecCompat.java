/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.jni;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.system.StructTimespec;

public final class StructTimespecCompat {

    public final long tv_sec; /*time_t*/
    public final long tv_nsec;

    public StructTimespecCompat(long tv_sec, long tv_nsec) {
        this.tv_sec = tv_sec;
        this.tv_nsec = tv_nsec;
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    public static StructTimespecCompat fromStructTimespec(StructTimespec structTimespec) {
        return new StructTimespecCompat(structTimespec.tv_sec, structTimespec.tv_nsec);
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    public StructTimespec toStructTimespec() {
        return new StructTimespec(tv_sec, tv_nsec);
    }
}
