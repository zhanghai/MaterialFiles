/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.app

import android.os.AsyncTask
import android.os.Build
import android.webkit.WebView
import com.facebook.stetho.Stetho
import com.jakewharton.threetenabp.AndroidThreeTen
import jcifs.context.SingletonContext
import me.zhanghai.android.files.BuildConfig
import me.zhanghai.android.files.coil.initializeCoil
import me.zhanghai.android.files.filejob.fileJobNotificationTemplate
import me.zhanghai.android.files.ftpserver.ftpServerServiceNotificationTemplate
import me.zhanghai.android.files.hiddenapi.HiddenApi
import me.zhanghai.android.files.provider.FileSystemProviders
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.storage.FtpServerAuthenticator
import me.zhanghai.android.files.storage.SftpServerAuthenticator
import me.zhanghai.android.files.storage.SmbServerAuthenticator
import me.zhanghai.android.files.storage.StorageVolumeListLiveData
import me.zhanghai.android.files.theme.custom.CustomThemeHelper
import me.zhanghai.android.files.theme.night.NightModeHelper
import java.util.Properties
import me.zhanghai.android.files.provider.ftp.client.Client as FtpClient
import me.zhanghai.android.files.provider.sftp.client.Client as SftpClient
import me.zhanghai.android.files.provider.smb.client.Client as SmbClient

val appInitializers = listOf(
    ::initializeCrashlytics, ::disableHiddenApiChecks, ::initializeThreeTen,
    ::initializeWebViewDebugging, ::initializeStetho, ::initializeCoil,
    ::initializeFileSystemProviders, ::upgradeApp, ::initializeLiveDataObjects,
    ::initializeCustomTheme, ::initializeNightMode, ::createNotificationChannels
)

private fun initializeCrashlytics() {
//#ifdef NONFREE
    me.zhanghai.android.files.nonfree.CrashlyticsInitializer.initialize()
//#endif
}

private fun disableHiddenApiChecks() {
    HiddenApi.disableHiddenApiChecks()
}

private fun initializeThreeTen() {
    AndroidThreeTen.init(application)
}

private fun initializeWebViewDebugging() {
    if (BuildConfig.DEBUG) {
        WebView.setWebContentsDebuggingEnabled(true)
    }
}

private fun initializeStetho() {
    Stetho.initializeWithDefaults(application)
}

private fun initializeFileSystemProviders() {
    FileSystemProviders.install()
    FileSystemProviders.overflowWatchEvents = true
    // SingletonContext.init() calls NameServiceClientImpl.initCache() which connects to network.
    AsyncTask.THREAD_POOL_EXECUTOR.execute {
        SingletonContext.init(
            Properties().apply {
                setProperty("jcifs.netbios.cachePolicy", "0")
                setProperty("jcifs.smb.client.maxVersion", "SMB1")
            }
        )
    }
    FtpClient.authenticator = FtpServerAuthenticator
    SftpClient.authenticator = SftpServerAuthenticator
    SmbClient.authenticator = SmbServerAuthenticator
}

private fun initializeLiveDataObjects() {
    // Force initialization of LiveData objects so that it won't happen on a background thread.
    StorageVolumeListLiveData.value
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
