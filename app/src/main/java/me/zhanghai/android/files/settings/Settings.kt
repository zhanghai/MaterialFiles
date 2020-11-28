/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings

import android.os.Environment
import java8.nio.file.Path
import java8.nio.file.Paths
import me.zhanghai.android.files.R
import me.zhanghai.android.files.app.application
import me.zhanghai.android.files.compat.EnvironmentCompat2
import me.zhanghai.android.files.filelist.FileSortOptions
import me.zhanghai.android.files.filelist.LongFilenameEllipsizeStyle
import me.zhanghai.android.files.filelist.OpenApkDefaultAction
import me.zhanghai.android.files.navigation.BookmarkDirectory
import me.zhanghai.android.files.navigation.StandardDirectorySettings
import me.zhanghai.android.files.provider.root.RootStrategy
import me.zhanghai.android.files.theme.custom.ThemeColor
import me.zhanghai.android.files.theme.night.NightMode
import java.io.File

object Settings {
    val FILE_LIST_DEFAULT_DIRECTORY: SettingLiveData<Path> =
        ParcelValueSettingLiveData(
            R.string.pref_key_file_list_default_directory,
            Paths.get(Environment.getExternalStorageDirectory().absolutePath)
        )

    val FILE_LIST_PERSISTENT_DRAWER_OPEN: SettingLiveData<Boolean> =
        BooleanSettingLiveData(
            R.string.pref_key_file_list_persistent_drawer_open,
            R.bool.pref_default_value_file_list_persistent_drawer_open
        )

    val FILE_LIST_SHOW_HIDDEN_FILES: SettingLiveData<Boolean> =
        BooleanSettingLiveData(
            R.string.pref_key_file_list_show_hidden_files,
            R.bool.pref_default_value_file_list_show_hidden_files
        )

    val FILE_LIST_SORT_OPTIONS: SettingLiveData<FileSortOptions> =
        ParcelValueSettingLiveData(
            R.string.pref_key_file_list_sort_options,
            FileSortOptions(FileSortOptions.By.NAME, FileSortOptions.Order.ASCENDING, true)
        )

    val CREATE_ARCHIVE_TYPE: SettingLiveData<Int> =
        ResourceIdSettingLiveData(R.string.pref_key_create_archive_type, R.id.zipRadio)

    val FTP_SERVER_ANONYMOUS_LOGIN: SettingLiveData<Boolean> =
        BooleanSettingLiveData(
            R.string.pref_key_ftp_server_anonymous_login,
            R.bool.pref_default_value_ftp_server_anonymous_login
        )

    val FTP_SERVER_USERNAME: SettingLiveData<String> =
        StringSettingLiveData(
            R.string.pref_key_ftp_server_username, R.string.pref_default_value_ftp_server_username
        )

    val FTP_SERVER_PASSWORD: SettingLiveData<String> =
        StringSettingLiveData(
            R.string.pref_key_ftp_server_password, R.string.pref_default_value_empty
        )

    val FTP_SERVER_PORT: SettingLiveData<Int> =
        IntegerSettingLiveData(
            R.string.pref_key_ftp_server_port, R.integer.pref_default_value_ftp_server_port
        )

    val FTP_SERVER_HOME_DIRECTORY: SettingLiveData<Path> =
        ParcelValueSettingLiveData(
            R.string.pref_key_ftp_server_home_directory,
            Paths.get(Environment.getExternalStorageDirectory().absolutePath)
        )

    val FTP_SERVER_WRITABLE: SettingLiveData<Boolean> =
        BooleanSettingLiveData(
            R.string.pref_key_ftp_server_writable, R.bool.pref_default_value_ftp_server_writable
        )

    val THEME_COLOR: SettingLiveData<ThemeColor> =
        EnumSettingLiveData(
            R.string.pref_key_theme_color, R.string.pref_default_value_theme_color,
            ThemeColor::class.java
        )

    val NIGHT_MODE: SettingLiveData<NightMode> =
        EnumSettingLiveData(
            R.string.pref_key_night_mode, R.string.pref_default_value_night_mode,
            NightMode::class.java
        )

    val BLACK_NIGHT_MODE: SettingLiveData<Boolean> =
        BooleanSettingLiveData(
            R.string.pref_key_black_night_mode, R.bool.pref_default_value_black_night_mode
        )

    val FILE_LIST_ANIMATION: SettingLiveData<Boolean> =
        BooleanSettingLiveData(
            R.string.pref_key_file_list_animation, R.bool.pref_default_value_file_list_animation
        )

    val LONG_FILENAME_ELLIPSIZE_STYLE:SettingLiveData<LongFilenameEllipsizeStyle> =
        EnumSettingLiveData(
            R.string.pref_key_long_filename_ellipsize_style,R.string.pref_default_value_long_filename_ellipsize_style,
            LongFilenameEllipsizeStyle::class.java
        )

    val STANDARD_DIRECTORY_SETTINGS: SettingLiveData<List<StandardDirectorySettings>> =
        ParcelValueSettingLiveData(R.string.pref_key_standard_directory_settings, emptyList())

    val BOOKMARK_DIRECTORIES: SettingLiveData<List<BookmarkDirectory>> =
        ParcelValueSettingLiveData(
            R.string.pref_key_bookmark_directories, listOf(
                BookmarkDirectory(
                    application.getString(R.string.settings_bookmark_directory_screenshots),
                    Paths.get(
                        File(
                            @Suppress("DEPRECATION")
                            Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES
                            ), EnvironmentCompat2.DIRECTORY_SCREENSHOTS
                        ).absolutePath
                    )
                )
            )
        )

    val ROOT_STRATEGY: SettingLiveData<RootStrategy> =
        EnumSettingLiveData(
            R.string.pref_key_root_strategy, R.string.pref_default_value_root_strategy,
            RootStrategy::class.java
        )

    val ARCHIVE_FILE_NAME_ENCODING: SettingLiveData<String> =
        StringSettingLiveData(
            R.string.pref_key_archive_file_name_encoding,
            R.string.pref_default_value_archive_file_name_encoding
        )

    val OPEN_APK_DEFAULT_ACTION: SettingLiveData<OpenApkDefaultAction> =
        EnumSettingLiveData(
            R.string.pref_key_open_apk_default_action,
            R.string.pref_default_value_open_apk_default_action,
            OpenApkDefaultAction::class.java
        )

    val READ_REMOTE_FILES_FOR_THUMBNAIL: SettingLiveData<Boolean> =
        BooleanSettingLiveData(
            R.string.pref_key_read_remote_files_for_thumbnail,
            R.bool.pref_default_value_read_remote_files_for_thumbnail
        )
}
