/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import java8.nio.file.Path
import me.zhanghai.android.files.settings.PathPreference
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.valueCompat

class FtpServerHomeDirectoryPreference : PathPreference {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    override var persistedPath: Path
        get() = Settings.FTP_SERVER_HOME_DIRECTORY.valueCompat
        set(value) {
            Settings.FTP_SERVER_HOME_DIRECTORY.putValue(value)
        }
}
