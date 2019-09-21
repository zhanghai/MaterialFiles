/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import java.io.IOException;

public interface SetReadOnlyFileStore {

    void setReadOnly(boolean readOnly) throws IOException;
}
