/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.jni;

public final class StructGroup {

    public final String gr_name;
    public final String gr_passwd;
    public final int gr_gid;
    public final String[] gr_mem;

    public StructGroup(String gr_name, String gr_passwd, int gr_gid, String[] gr_mem) {
        this.gr_name = gr_name;
        this.gr_passwd = gr_passwd;
        this.gr_gid = gr_gid;
        this.gr_mem = gr_mem;
    }
}
