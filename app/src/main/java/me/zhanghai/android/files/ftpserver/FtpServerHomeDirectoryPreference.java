/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import me.zhanghai.android.files.settings.PathPreference;
import me.zhanghai.android.files.settings.Settings;

public class FtpServerHomeDirectoryPreference extends PathPreference {

    public FtpServerHomeDirectoryPreference(@NonNull Context context) {
        super(context);
    }

    public FtpServerHomeDirectoryPreference(@NonNull Context context,
                                            @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FtpServerHomeDirectoryPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FtpServerHomeDirectoryPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected Path getPersistedPath() {
        return Settings.FTP_SERVER_HOME_DIRECTORY.getValue();
    }

    @Override
    protected void persistPath(Path path) {
        Settings.FTP_SERVER_HOME_DIRECTORY.putValue(path);
    }
}
