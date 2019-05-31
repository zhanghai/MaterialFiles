/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.syscall;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.provider.common.ByteString;

public final class StructDirent {

    public final long d_ino; /*ino_t*/
    public final long d_off; /*off64_t*/
    public final int d_reclen; /*unsigned short*/
    public final int d_type; /*unsigned char*/
    @NonNull
    public final ByteString d_name;

    public StructDirent(long d_ino, long d_off, int d_reclen, int d_type,
                        @NonNull ByteString d_name) {
        this.d_ino = d_ino;
        this.d_off = d_off;
        this.d_reclen = d_reclen;
        this.d_type = d_type;
        this.d_name = d_name;
    }
}
