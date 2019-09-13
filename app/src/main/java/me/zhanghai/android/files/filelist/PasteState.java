/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import java.util.LinkedHashSet;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.file.FileItem;

public class PasteState {

    public boolean copy;

    @NonNull
    public final LinkedHashSet<FileItem> files = new LinkedHashSet<>();
}
