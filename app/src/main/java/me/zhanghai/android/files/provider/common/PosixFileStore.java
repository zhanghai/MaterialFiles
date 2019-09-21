/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.io.IOException;

public abstract class PosixFileStore extends AbstractFileStore {

    public abstract void refresh() throws IOException;

    public abstract void setReadOnly(boolean readOnly) throws IOException;
}
