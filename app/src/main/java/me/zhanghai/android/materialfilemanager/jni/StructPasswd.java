/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.jni;

public final class StructPasswd {

    public final String pw_name;
    public final int pw_uid;
    public final int pw_gid;
    public final String pw_gecos;
    public final String pw_dir;
    public final String pw_shell;

    public StructPasswd(String pw_name, int pw_uid, int pw_gid, String pw_gecos, String pw_dir,
                        String pw_shell) {
        this.pw_name = pw_name;
        this.pw_uid = pw_uid;
        this.pw_gid = pw_gid;
        this.pw_gecos = pw_gecos;
        this.pw_dir = pw_dir;
        this.pw_shell = pw_shell;
    }
}
