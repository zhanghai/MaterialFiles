/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import java.util.List;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.file.MimeTypes;
import me.zhanghai.java.functional.Functional;

public class PickOptions {

    public final boolean readOnly;
    public final boolean pickDirectory;
    @NonNull
    public final List<String> mimeTypes;
    public final boolean localOnly;
    public final boolean allowMultiple;

    public PickOptions(boolean readOnly, boolean pickDirectory, @NonNull List<String> mimeTypes,
                       boolean localOnly, boolean allowMultiple) {
        this.readOnly = readOnly;
        this.pickDirectory = pickDirectory;
        this.mimeTypes = mimeTypes;
        this.localOnly = localOnly;
        this.allowMultiple = allowMultiple;
    }

    public boolean mimeTypeMatches(@NonNull String mimeType) {
        return Functional.some(mimeTypes, mimeTypeSpec -> MimeTypes.matches(mimeTypeSpec,
                mimeType));
    }
}
