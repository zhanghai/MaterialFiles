/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.app

import android.os.Build
import com.facebook.stetho.Stetho
import com.jakewharton.threetenabp.AndroidThreeTen
import me.zhanghai.android.files.compat.RestrictedHiddenApiAccess
import me.zhanghai.android.files.filejob.fileJobNotificationTemplate
import me.zhanghai.android.files.ftpserver.ftpServerServiceNotificationTemplate
import me.zhanghai.android.files.provider.FileSystemProviders
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.theme.custom.CustomThemeHelper
import me.zhanghai.android.files.theme.night.NightModeHelper

val appInitializers = listOf(
    ::initializeCrashlytics, ::allowRestrictedHiddenApiAccess, ::initializeThreeTen,
    ::initializeStetho, ::initializeFileSystemProviders, ::initializeSettings,
    ::initializeCustomTheme, ::initializeNightMode, ::createNotificationChannels
)

private fun initializeCrashlytics() {
//#ifdef NONFREE
    me.zhanghai.android.files.nonfree.CrashlyticsInitializer.initialize()
//#endif
}

private fun allowRestrictedHiddenApiAccess() {
    RestrictedHiddenApiAccess.allow()
}

private fun initializeThreeTen() {
    AndroidThreeTen.init(application)
}

private fun initializeStetho() {
    Stetho.initializeWithDefaults(application)
}

private fun initializeFileSystemProviders() {
    FileSystemProviders.install()
    FileSystemProviders.overflowWatchEvents = true
}

private fun initializeSettings() {
    // Force initialization of Settings so that it won't happen on a background thread.
    Settings.FILE_LIST_DEFAULT_DIRECTORY.value
}

private fun initializeCustomTheme() {
    CustomThemeHelper.initialize(application)
}

private fun initializeNightMode() {
    NightModeHelper.initialize(application)
}

private fun createNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        notificationManager.createNotificationChannels(
            listOf(
                backgroundActivityStartNotificationTemplate.channelTemplate,
                fileJobNotificationTemplate.channelTemplate,
                ftpServerServiceNotificationTemplate.channelTemplate
            ).map { it.create(application) }
        )
    }
}
