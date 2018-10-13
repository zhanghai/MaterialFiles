/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import me.zhanghai.android.materialfilemanager.AppApplication;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.about.AboutActivity;
import me.zhanghai.android.materialfilemanager.file.FormatUtils;
import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.filesystem.Files;
import me.zhanghai.android.materialfilemanager.filesystem.JavaFile;
import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.settings.SettingsActivity;
import me.zhanghai.android.materialfilemanager.util.ListBuilder;
import me.zhanghai.android.materialfilemanager.util.StorageManagerCompat;
import me.zhanghai.android.materialfilemanager.util.StorageVolumeCompat;

public class NavigationItems {

    private static final List<Pair<String, Integer>> DEFAULT_FAVORITE_DIRECTORIES =
            ListBuilder.<Pair<String, Integer>>newArrayList()
                    .add(new Pair<>(Environment.DIRECTORY_DCIM, R.drawable.camera_icon_white_24dp))
                    .add(new Pair<>(Environment.DIRECTORY_DOCUMENTS,
                            R.drawable.document_icon_white_24dp))
                    .add(new Pair<>(Environment.DIRECTORY_DOWNLOADS,
                            R.drawable.download_icon_white_24dp))
                    .add(new Pair<>(Environment.DIRECTORY_MOVIES, R.drawable.video_icon_white_24dp))
                    .add(new Pair<>(Environment.DIRECTORY_MUSIC, R.drawable.audio_icon_white_24dp))
                    .add(new Pair<>(Environment.DIRECTORY_PICTURES,
                            R.drawable.image_icon_white_24dp))
                    .buildUnmodifiable();

    @NonNull
    public static List<NavigationItem> getItems() {
        List<NavigationItem> items = new ArrayList<>();
        items.addAll(getRootItems());
        List<NavigationItem> favoriteItems = getFavoriteItems();
        if (!favoriteItems.isEmpty()) {
            items.add(null);
            items.addAll(favoriteItems);
        }
        items.add(null);
        items.addAll(getMenuItems());
        return items;
    }

    @NonNull
    @Size(min = 1)
    private static List<NavigationItem> getRootItems() {
        List<NavigationItem> rootItems = new ArrayList<>();
        rootItems.add(new RootDirectoryRootItem());
        StorageManager storageManager = ContextCompat.getSystemService(AppApplication.getInstance(),
                StorageManager.class);
        List<StorageVolume> storageVolumes = StorageManagerCompat.getStorageVolumes(storageManager);
        Functional.map(storageVolumes, StorageVolumeRootItem::new, rootItems);
        // TODO: Add root action item
        return rootItems;
    }

    @NonNull
    @Size(min = 0)
    private static List<NavigationItem> getFavoriteItems() {
        List<NavigationItem> favoriteItems = new ArrayList<>();
        for (Pair<String, Integer> directory : DEFAULT_FAVORITE_DIRECTORIES) {
            String path = Environment.getExternalStoragePublicDirectory(directory.first).getPath();
            int iconRes = directory.second;
            favoriteItems.add(new FavoriteItem(Files.ofLocalPath(path), iconRes));
        }
        // TODO: Persist and load favorites.
        return favoriteItems;
    }

    @NonNull
    @Size(2)
    private static List<NavigationItem> getMenuItems() {
        Context context = AppApplication.getInstance();
        return Arrays.asList(
                new ActivityMenuItem(R.drawable.settings_icon_white_24dp,
                        R.string.navigation_settings, SettingsActivity.class, context),
                new ActivityMenuItem(R.drawable.about_icon_white_24dp, R.string.navigation_about,
                        AboutActivity.class, context)
        );
    }

    private static abstract class FileItem extends NavigationItem {

        @NonNull
        protected File mFile;

        public FileItem(@NonNull File file) {
            mFile = file;
        }

        @Override
        public long getId() {
            // Items of different types may point to the same file.
            return Objects.hash(getClass(), mFile.getUri());
        }

        @Override
        public boolean isChecked(@NonNull Listener listener) {
            return Objects.equals(listener.getCurrentFile().getUri(), mFile.getUri());
        }

        @Override
        public void onClick(@NonNull Listener listener) {
            listener.navigateToFile(mFile);
            listener.closeNavigationDrawer();
        }
    }

    private static abstract class LocalRootItem extends FileItem {

        @DrawableRes
        private int mIconRes;
        private long mFreeSpace;
        private long mTotalSpace;

        public LocalRootItem(@NonNull String path, @DrawableRes int iconRes) {
            super(Files.ofLocalPath(path));

            mIconRes = iconRes;
            mFreeSpace = JavaFile.getFreeSpace(path);
            mTotalSpace = JavaFile.getTotalSpace(path);
        }

        @DrawableRes
        @Override
        public int getIconRes() {
            return mIconRes;
        }

        @Nullable
        @Override
        public String getSubtitle(@NonNull Context context) {
            String freeSpace = FormatUtils.formatHumanReadableSize(mFreeSpace, context);
            String totalSpace = FormatUtils.formatHumanReadableSize(mTotalSpace, context);
            return context.getString(R.string.navigation_root_subtitle_format, freeSpace,
                    totalSpace);
        }
    }

    public static class RootDirectoryRootItem extends LocalRootItem {

        public RootDirectoryRootItem() {
            super("/", R.drawable.device_icon_white_24dp);
        }

        @StringRes
        @Override
        public int getTitleRes() {
            return R.string.navigation_root_directory;
        }
    }

    public static class StorageVolumeRootItem extends LocalRootItem {

        private StorageVolume mStorageVolume;

        public StorageVolumeRootItem(@NonNull StorageVolume storageVolume) {
            super(StorageVolumeCompat.getPath(storageVolume), R.drawable.sd_card_icon_white_24dp);

            mStorageVolume = storageVolume;
        }

        @NonNull
        @Override
        public String getTitle(@NonNull Context context) {
            return StorageVolumeCompat.getDescription(mStorageVolume, context);
        }

        @Override
        public int getTitleRes() {
            throw new UnsupportedOperationException();
        }
    }

    public static class FavoriteItem extends FileItem {

        @DrawableRes
        private int mIconRes;

        public FavoriteItem(@NonNull File file, @DrawableRes int iconRes) {
            super(file);

            mIconRes = iconRes;
        }

        @DrawableRes
        @Override
        public int getIconRes() {
            return mIconRes;
        }

        @NonNull
        @Override
        public String getTitle(@NonNull Context context) {
            return mFile.getName();
        }

        @Override
        protected int getTitleRes() {
            throw new UnsupportedOperationException();
        }
    }

    private abstract static class MenuItem extends NavigationItem {

        @DrawableRes
        private int mIconRes;
        @StringRes
        private int mTitleRes;

        public MenuItem(@DrawableRes int iconRes, @StringRes int titleRes) {
            mIconRes = iconRes;
            mTitleRes = titleRes;
        }

        @DrawableRes
        @Override
        public int getIconRes() {
            return mIconRes;
        }

        @StringRes
        @Override
        public int getTitleRes() {
            return mTitleRes;
        }
    }

    private static class ActivityMenuItem extends MenuItem {

        @NonNull
        private Intent mIntent;

        public ActivityMenuItem(@DrawableRes int iconRes, @StringRes int titleRes,
                                @NonNull Intent intent) {
            super(iconRes, titleRes);

            mIntent = intent;
        }

        public ActivityMenuItem(@DrawableRes int iconRes, @StringRes int titleRes,
                                @NonNull Class<? extends Activity> activityClass,
                                @NonNull Context context) {
            this(iconRes, titleRes, new Intent(context, activityClass));
        }

        @Override
        public long getId() {
            return mIntent.getComponent().hashCode();
        }

        @Override
        public void onClick(@NonNull Listener listener) {
            listener.startActivity(mIntent);
            listener.closeNavigationDrawer();
        }
    }
}
