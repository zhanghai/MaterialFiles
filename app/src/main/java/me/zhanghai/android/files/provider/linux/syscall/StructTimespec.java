/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.syscall;

/**
 * @see android.system.StructTimespec
 */
public final class StructTimespec {

    public final long tv_sec; /*time_t*/
    public final long tv_nsec;

    public StructTimespec(long tv_sec, long tv_nsec) {
        this.tv_sec = tv_sec;
        this.tv_nsec = tv_nsec;
    }
}
