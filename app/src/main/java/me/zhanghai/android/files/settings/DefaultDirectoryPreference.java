/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;

public class DefaultDirectoryPreference extends PathPreference {

    public DefaultDirectoryPreference(@NonNull Context context) {
        super(context);
    }

    public DefaultDirectoryPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DefaultDirectoryPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                      int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DefaultDirectoryPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                      int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected Path getPersistedPath() {
        return Settings.FILE_LIST_DEFAULT_DIRECTORY.getValue();
    }

    @Override
    protected void persistPath(Path path) {
        Settings.FILE_LIST_DEFAULT_DIRECTORY.putValue(path);
    }
}
