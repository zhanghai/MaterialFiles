/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import me.zhanghai.android.files.R
import me.zhanghai.android.files.app.application

private const val ID = "recent_files"

/**
 * Adds or removes the launcher shortcut for recent files, so that it follows the setting for the
 * navigation drawer item.
 *
 * A shortcut the user has already pinned keeps working either way, which is why opening recent
 * files never depends on the setting itself.
 */
fun updateRecentFilesShortcut(isEnabled: Boolean) {
    if (!isEnabled) {
        ShortcutManagerCompat.removeDynamicShortcuts(application, listOf(ID))
        return
    }
    val shortcutInfo = ShortcutInfoCompat.Builder(application, ID)
        .setShortLabel(application.getString(R.string.shortcut_recent_files_title))
        .setIcon(IconCompat.createWithResource(application, R.mipmap.recent_files_shortcut_icon))
        .setIntent(FileListActivity.createViewRecentFilesIntent())
        .build()
    ShortcutManagerCompat.pushDynamicShortcut(application, shortcutInfo)
}
