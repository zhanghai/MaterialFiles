/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.root;

public interface RootablePath {

    boolean canUseRoot();

    boolean preferUseRoot();

    boolean shouldUseRoot();

    void setUseRoot();
}
