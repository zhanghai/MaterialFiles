/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import me.zhanghai.android.files.AppApplication;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.about.AboutActivity;
import me.zhanghai.android.files.file.FormatUtils;
import me.zhanghai.android.files.filesystem.File;
import me.zhanghai.android.files.filesystem.Files;
import me.zhanghai.android.files.filesystem.JavaFile;
import me.zhanghai.android.files.functional.Functional;
import me.zhanghai.android.files.settings.SettingsActivity;
import me.zhanghai.android.files.util.ListBuilder;
import me.zhanghai.android.files.util.StorageManagerCompat;
import me.zhanghai.android.files.util.StorageVolumeCompat;

public class NavigationItems {

    // @see android.os.Environment#STANDARD_DIRECTORIES
    private static final List<Pair<String, Integer>> STANDARD_DIRECTORIES =
            ListBuilder.<Pair<String, Integer>>newArrayList()
                    .add(new Pair<>(Environment.DIRECTORY_ALARMS, R.drawable.alarm_icon_white_24dp))
                    .add(new Pair<>(Environment.DIRECTORY_DCIM, R.drawable.camera_icon_white_24dp))
                    .add(new Pair<>(Environment.DIRECTORY_DOCUMENTS,
                            R.drawable.document_icon_white_24dp))
                    .add(new Pair<>(Environment.DIRECTORY_DOWNLOADS,
                            R.drawable.download_icon_white_24dp))
                    .add(new Pair<>(Environment.DIRECTORY_MOVIES, R.drawable.video_icon_white_24dp))
                    .add(new Pair<>(Environment.DIRECTORY_MUSIC, R.drawable.audio_icon_white_24dp))
                    .add(new Pair<>(Environment.DIRECTORY_NOTIFICATIONS,
                            R.drawable.notification_icon_white_24dp))
                    .add(new Pair<>(Environment.DIRECTORY_PICTURES,
                            R.drawable.image_icon_white_24dp))
                    .add(new Pair<>(Environment.DIRECTORY_PODCASTS,
                            R.drawable.podcast_icon_white_24dp))
                    .add(new Pair<>(Environment.DIRECTORY_RINGTONES,
                            R.drawable.ringtone_icon_white_24dp))
                    .add(new Pair<>("tencent/QQfile_recv", R.drawable.qq_icon_white_24dp))
                    .add(new Pair<>("tencent/MicroMsg/Download", R.drawable.wechat_icon_white_24dp))
                    .buildUnmodifiable();

    @NonNull
    public static List<NavigationItem> getItems() {
        List<NavigationItem> items = new ArrayList<>();
        items.addAll(getRootItems());
        List<NavigationItem> standardDirectoryItems = getStandardDirectoryItems();
        if (!standardDirectoryItems.isEmpty()) {
            items.add(null);
            items.addAll(standardDirectoryItems);
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
        rootItems.add(new AddDocumentTreeItem());
        return rootItems;
    }

    @NonNull
    @Size(min = 0)
    private static List<NavigationItem> getStandardDirectoryItems() {
        List<NavigationItem> favoriteItems = new ArrayList<>();
        for (Pair<String, Integer> directory : STANDARD_DIRECTORIES) {
            String path = Environment.getExternalStoragePublicDirectory(directory.first).getPath();
            int iconRes = directory.second;
            favoriteItems.add(new StandardDirectoryItem(Files.ofLocalPath(path), iconRes));
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
        private final File mFile;

        public FileItem(@NonNull File file) {
            mFile = file;
        }

        @NonNull
        public File getFile() {
            return mFile;
        }

        @Override
        public long getId() {
            // Items of different types may point to the same file.
            return Objects.hash(getClass(), mFile);
        }

        @Override
        public boolean isChecked(@NonNull Listener listener) {
            return Objects.equals(listener.getCurrentFile(), mFile);
        }

        @Override
        public void onClick(@NonNull Listener listener) {
            listener.navigateToFile(mFile);
            listener.closeNavigationDrawer();
        }
    }

    private static abstract class RootItem extends FileItem implements NavigationRoot {

        public RootItem(@NonNull File file) {
            super(file);
        }

        @Override
        public void onClick(@NonNull Listener listener) {
            listener.navigateToRoot(getFile());
            listener.closeNavigationDrawer();
        }

        @NonNull
        @Override
        public String getName(@NonNull Context context) {
            return getTitle(context);
        }
    }

    private static abstract class LocalRootItem extends RootItem {

        @DrawableRes
        private final int mIconRes;
        private final long mFreeSpace;
        private final long mTotalSpace;

        public LocalRootItem(@NonNull String path, @DrawableRes int iconRes) {
            super(Files.ofLocalPath(path));

            mIconRes = iconRes;
            long totalSpace = JavaFile.getTotalSpace(path);
            if (totalSpace != 0) {
                mFreeSpace = JavaFile.getFreeSpace(path);
                mTotalSpace = totalSpace;
            } else {
                // Root directory may not be an actual partition on legacy Android versions (can be
                // a ramdisk instead). On modern Android the system partition will be mounted as
                // root instead so let's try with the system partition again.
                // @see https://source.android.com/devices/bootloader/system-as-root
                String systemPath = Environment.getRootDirectory().getPath();
                mFreeSpace = JavaFile.getFreeSpace(systemPath);
                mTotalSpace = JavaFile.getTotalSpace(systemPath);
            }
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

    private static class RootDirectoryRootItem extends LocalRootItem {

        public RootDirectoryRootItem() {
            super("/", R.drawable.device_icon_white_24dp);
        }

        @StringRes
        @Override
        public int getTitleRes() {
            return R.string.navigation_root_directory;
        }
    }

    private static class StorageVolumeRootItem extends LocalRootItem {

        @NonNull
        private final StorageVolume mStorageVolume;

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

    private static class AddDocumentTreeItem extends NavigationItem {

        @Override
        public long getId() {
            return R.string.navigation_add_document_tree;
        }

        @DrawableRes
        @Override
        protected int getIconRes() {
            return R.drawable.add_icon_white_24dp;
        }

        @StringRes
        @Override
        protected int getTitleRes() {
            return R.string.navigation_add_document_tree;
        }

        @Override
        public void onClick(@NonNull Listener listener) {
            listener.onAddDocumentTree();
        }
    }

    private static class StandardDirectoryItem extends FileItem {

        @DrawableRes
        private final int mIconRes;

        public StandardDirectoryItem(@NonNull File file, @DrawableRes int iconRes) {
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
            return getFile().getName();
        }

        @Override
        protected int getTitleRes() {
            throw new UnsupportedOperationException();
        }
    }

    private abstract static class MenuItem extends NavigationItem {

        @DrawableRes
        private final int mIconRes;
        @StringRes
        private final int mTitleRes;

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
        private final Intent mIntent;

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
