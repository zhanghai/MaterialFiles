/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.os.Environment;
import android.os.Parcelable;

import java.util.Collections;
import java.util.List;

import java8.nio.file.Path;
import java8.nio.file.Paths;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.filelist.FileSortOptions;
import me.zhanghai.android.files.filelist.OpenApkDefaultAction;
import me.zhanghai.android.files.navigation.BookmarkDirectory;
import me.zhanghai.android.files.navigation.StandardDirectorySettings;
import me.zhanghai.android.files.provider.root.RootStrategy;
import me.zhanghai.android.files.settings.SettingLiveDatas.BooleanSettingLiveData;
import me.zhanghai.android.files.settings.SettingLiveDatas.EnumSettingLiveData;
import me.zhanghai.android.files.settings.SettingLiveDatas.ParcelableListSettingLiveData;
import me.zhanghai.android.files.settings.SettingLiveDatas.ParcelableSettingLiveData;
import me.zhanghai.android.files.settings.SettingLiveDatas.ResourceIdSettingLiveData;
import me.zhanghai.android.files.settings.SettingLiveDatas.StringSettingLiveData;
import me.zhanghai.android.files.theme.custom.CustomThemeColors;
import me.zhanghai.android.files.theme.night.NightMode;

public interface Settings {

    @SuppressWarnings("unchecked")
    SettingLiveData<Path> FILE_LIST_DEFAULT_PATH = (SettingLiveData<Path>) (SettingLiveData<?>)
            new ParcelableSettingLiveData<>(R.string.pref_key_file_list_default_path, (Parcelable)
                    Paths.get(Environment.getExternalStorageDirectory().getAbsolutePath()),
                    (Class<Parcelable>) (Class<?>) Path.class);

    SettingLiveData<Boolean> FILE_LIST_PERSISTENT_DRAWER_OPEN = new BooleanSettingLiveData(
            R.string.pref_key_file_list_persistent_drawer_open,
            R.bool.pref_default_value_file_list_persistent_drawer_open);

    SettingLiveData<Boolean> FILE_LIST_SHOW_HIDDEN_FILES = new BooleanSettingLiveData(
            R.string.pref_key_file_list_show_hidden_files,
            R.bool.pref_default_value_file_list_show_hidden_files);

    SettingLiveData<FileSortOptions> FILE_LIST_SORT_OPTIONS = new ParcelableSettingLiveData<>(
            R.string.pref_key_file_list_sort_options, new FileSortOptions(FileSortOptions.By.NAME,
            FileSortOptions.Order.ASCENDING, true), FileSortOptions.class);

    SettingLiveData<Integer> CREATE_ARCHIVE_TYPE = new ResourceIdSettingLiveData(
            R.string.pref_key_create_archive_type, R.id.type_zip);

    SettingLiveData<CustomThemeColors.Primary> PRIMARY_COLOR = new EnumSettingLiveData<>(
            R.string.pref_key_primary_color, R.string.pref_default_value_primary_color,
            CustomThemeColors.Primary.class);

    SettingLiveData<CustomThemeColors.Accent> ACCENT_COLOR = new EnumSettingLiveData<>(
            R.string.pref_key_accent_color, R.string.pref_default_value_accent_color,
            CustomThemeColors.Accent.class);
 
    SettingLiveData<NightMode> NIGHT_MODE = new EnumSettingLiveData<>(R.string.pref_key_night_mode,
            R.string.pref_default_value_night_mode, NightMode.class);

    SettingLiveData<Boolean> FILE_LIST_ANIMATION = new BooleanSettingLiveData(
            R.string.pref_key_file_list_animation, R.bool.pref_default_value_file_list_animation);

    SettingLiveData<List<StandardDirectorySettings>> STANDARD_DIRECTORY_SETTINGS =
            new ParcelableListSettingLiveData<>(R.string.pref_key_standard_directories, null,
                    StandardDirectorySettings.CREATOR);

    SettingLiveData<List<BookmarkDirectory>> BOOKMARK_DIRECTORIES =
            new ParcelableListSettingLiveData<>(R.string.pref_key_bookmark_directories,
                    Collections.emptyList(), BookmarkDirectory.CREATOR);

    SettingLiveData<RootStrategy> ROOT_STRATEGY = new EnumSettingLiveData<>(
            R.string.pref_key_root_strategy, R.string.pref_default_value_root_strategy,
            RootStrategy.class);

    SettingLiveData<String> ARCHIVE_FILE_NAME_ENCODING = new StringSettingLiveData(
            R.string.pref_key_archive_file_name_encoding,
            R.string.pref_default_value_archive_file_name_encoding);

    SettingLiveData<OpenApkDefaultAction> OPEN_APK_DEFAULT_ACTION = new EnumSettingLiveData<>(
            R.string.pref_key_open_apk_default_action,
            R.string.pref_default_value_open_apk_default_action, OpenApkDefaultAction.class);
}
