/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import java8.nio.file.Path;
import java8.nio.file.Paths;
import me.zhanghai.android.files.AppProvider;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.about.AboutActivity;
import me.zhanghai.android.files.compat.StorageManagerCompat;
import me.zhanghai.android.files.compat.StorageVolumeCompat;
import me.zhanghai.android.files.file.FormatUtils;
import me.zhanghai.android.files.ftpserver.FtpServerActivity;
import me.zhanghai.android.files.navigation.file.DocumentTree;
import me.zhanghai.android.files.navigation.file.JavaFile;
import me.zhanghai.android.files.provider.document.DocumentFileSystemProvider;
import me.zhanghai.android.files.settings.Settings;
import me.zhanghai.android.files.settings.SettingsActivity;
import me.zhanghai.android.files.util.ListBuilder;
import me.zhanghai.java.functional.Functional;

public class NavigationItems {

    // @see android.os.Environment#STANDARD_DIRECTORIES
    public static final List<StandardDirectory> DEFAULT_STANDARD_DIRECTORIES =
            ListBuilder.<StandardDirectory>newArrayList()
                    .add(new StandardDirectory(R.drawable.alarm_icon_white_24dp,
                            R.string.navigation_standard_directory_alarms,
                            Environment.DIRECTORY_ALARMS, false))
                    .add(new StandardDirectory(R.drawable.camera_icon_white_24dp,
                            R.string.navigation_standard_directory_dcim, Environment.DIRECTORY_DCIM,
                            true))
                    .add(new StandardDirectory(R.drawable.document_icon_white_24dp,
                            R.string.navigation_standard_directory_documents,
                            Environment.DIRECTORY_DOCUMENTS, false))
                    .add(new StandardDirectory(R.drawable.download_icon_white_24dp,
                            R.string.navigation_standard_directory_downloads,
                            Environment.DIRECTORY_DOWNLOADS, true))
                    .add(new StandardDirectory(R.drawable.video_icon_white_24dp,
                            R.string.navigation_standard_directory_movies,
                            Environment.DIRECTORY_MOVIES, true))
                    .add(new StandardDirectory(R.drawable.audio_icon_white_24dp,
                            R.string.navigation_standard_directory_music,
                            Environment.DIRECTORY_MUSIC, true))
                    .add(new StandardDirectory(R.drawable.notification_icon_white_24dp,
                            R.string.navigation_standard_directory_notifications,
                            Environment.DIRECTORY_NOTIFICATIONS, false))
                    .add(new StandardDirectory(R.drawable.image_icon_white_24dp,
                            R.string.navigation_standard_directory_pictures,
                            Environment.DIRECTORY_PICTURES, true))
                    .add(new StandardDirectory(R.drawable.podcast_icon_white_24dp,
                            R.string.navigation_standard_directory_podcasts,
                            Environment.DIRECTORY_PODCASTS, false))
                    .add(new StandardDirectory(R.drawable.ringtone_icon_white_24dp,
                            R.string.navigation_standard_directory_ringtones,
                            Environment.DIRECTORY_RINGTONES, false))
                    .add(new StandardDirectory(R.drawable.qq_icon_white_24dp,
                            R.string.navigation_standard_directory_qq, "tencent/QQfile_recv",
                            false))
                    .add(new StandardDirectory(R.drawable.tim_icon_white_24dp,
                            R.string.navigation_standard_directory_tim, "tencent/TIMfile_recv",
                            false))
                    .add(new StandardDirectory(R.drawable.wechat_icon_white_24dp,
                            R.string.navigation_standard_directory_wechat,
                            "tencent/MicroMsg/Download", false))
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
        List<NavigationItem> bookmarkDirectoryItems = getBookmarkDirectoryItems();
        if (!bookmarkDirectoryItems.isEmpty()) {
            items.add(null);
            items.addAll(bookmarkDirectoryItems);
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
        StorageManager storageManager = ContextCompat.getSystemService(AppProvider.requireContext(),
                StorageManager.class);
        List<StorageVolume> storageVolumes = StorageManagerCompat.getStorageVolumes(storageManager);
        Functional.map(storageVolumes, StorageVolumeRootItem::new, rootItems);
        List<Uri> treeUris = DocumentTreesLiveData.getInstance().getValue();
        Functional.map(treeUris, DocumentTreeRootItem::new, rootItems);
        rootItems.add(new AddDocumentTreeItem());
        return rootItems;
    }

    @NonNull
    @Size(min = 0)
    private static List<NavigationItem> getStandardDirectoryItems() {
        return Functional.map(Functional.filter(
                StandardDirectoriesLiveData.getInstance().getValue(), StandardDirectory::isEnabled),
                StandardDirectoryItem::new);
    }

    @NonNull
    public static List<StandardDirectory> getStandardDirectories() {
        Map<String, StandardDirectorySettings> settingsMap = new LinkedHashMap<>();
        for (StandardDirectorySettings settings : Settings.STANDARD_DIRECTORY_SETTINGS.getValue()) {
            settingsMap.put(settings.getId(), settings);
        }
        List<StandardDirectory> standardDirectories = getDefaultStandardDirectories();
        standardDirectories = Functional.map(standardDirectories, standardDirectory -> {
            StandardDirectorySettings settings = settingsMap.get(standardDirectory.getId());
            if (settings != null) {
                standardDirectory = standardDirectory.withSettings(settings);
            }
            return standardDirectory;
        });
        return standardDirectories;
    }

    @NonNull
    private static List<StandardDirectory> getDefaultStandardDirectories() {
        // HACK: Enable QQ and WeChat standard directories based on whether the directory exists.
        return Functional.map(DEFAULT_STANDARD_DIRECTORIES, standardDirectory -> {
            switch (standardDirectory.getIconRes()) {
                case R.drawable.qq_icon_white_24dp:
                case R.drawable.tim_icon_white_24dp:
                case R.drawable.wechat_icon_white_24dp: {
                    String path = getExternalStorageDirectory(standardDirectory.getRelativePath());
                    if (JavaFile.isDirectory(path)) {
                        return standardDirectory.withSettings(standardDirectory.toSettings()
                                .withEnabled(true));
                    }
                    break;
                }
            }
            return standardDirectory;
        });
    }

    @NonNull
    public static String getExternalStorageDirectory(@NonNull String relativePath) {
        return Environment.getExternalStoragePublicDirectory(relativePath).getPath();
    }

    @NonNull
    @Size(min = 0)
    private static List<NavigationItem> getBookmarkDirectoryItems() {
        return Functional.map(Settings.BOOKMARK_DIRECTORIES.getValue(), BookmarkDirectoryItem::new);
    }

    @NonNull
    @Size(2)
    private static List<NavigationItem> getMenuItems() {
        Context context = AppProvider.requireContext();
        return Arrays.asList(
                new ActivityMenuItem(R.drawable.shared_directory_icon_white_24dp,
                        R.string.navigation_ftp_server, FtpServerActivity.class, context),
                new ActivityMenuItem(R.drawable.settings_icon_white_24dp,
                        R.string.navigation_settings, SettingsActivity.class, context),
                new ActivityMenuItem(R.drawable.about_icon_white_24dp, R.string.navigation_about,
                        AboutActivity.class, context)
        );
    }

    private static abstract class FileItem extends NavigationItem {

        @NonNull
        private final Path mPath;

        public FileItem(@NonNull Path path) {
            mPath = path;
        }

        @NonNull
        public Path getPath() {
            return mPath;
        }

        @Override
        public long getId() {
            // Items of different types may point to the same file.
            return Objects.hash(getClass(), mPath);
        }

        @Override
        public boolean isChecked(@NonNull Listener listener) {
            return Objects.equals(listener.getCurrentPath(), mPath);
        }

        @Override
        public void onClick(@NonNull Listener listener) {
            listener.navigateTo(mPath);
            listener.closeNavigationDrawer();
        }
    }

    private static abstract class RootItem extends FileItem implements NavigationRoot {

        public RootItem(@NonNull Path path) {
            super(path);
        }

        @Override
        public void onClick(@NonNull Listener listener) {
            listener.navigateToRoot(getPath());
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
            super(Paths.get(path));

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
            throw new AssertionError();
        }
    }

    private static class DocumentTreeRootItem extends RootItem {

        @NonNull
        private final Uri mTreeUri;
        @NonNull
        private final String mTitle;

        public DocumentTreeRootItem(@NonNull Uri treeUri) {
            super(DocumentFileSystemProvider.getRootPathForTreeUri(treeUri));

            mTreeUri = treeUri;
            mTitle = DocumentTree.getDisplayName(mTreeUri, AppProvider.requireContext());
        }

        @DrawableRes
        @Override
        public int getIconRes() {
            return R.drawable.directory_icon_white_24dp;
        }

        @NonNull
        @Override
        public String getTitle(@NonNull Context context) {
            return mTitle;
        }

        @Override
        protected int getTitleRes() {
            throw new AssertionError();
        }

        @Override
        public boolean onLongClick(@NonNull Listener listener) {
            listener.onRemoveDocumentTree(mTreeUri);
            return true;
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

        private final StandardDirectory mStandardDirectory;

        public StandardDirectoryItem(@NonNull StandardDirectory standardDirectory) {
            super(Paths.get(getExternalStorageDirectory(standardDirectory.getRelativePath())));

            if (!standardDirectory.isEnabled()) {
                throw new IllegalArgumentException("StandardDirectory should be enabled");
            }

            mStandardDirectory = standardDirectory;
        }

        @DrawableRes
        @Override
        public int getIconRes() {
            return mStandardDirectory.getIconRes();
        }

        @NonNull
        @Override
        public String getTitle(@NonNull Context context) {
            return mStandardDirectory.getTitle(context);
        }

        @Override
        protected int getTitleRes() {
            throw new AssertionError();
        }
    }

    private static class BookmarkDirectoryItem extends FileItem {

        private final BookmarkDirectory mBookmarkDirectory;

        public BookmarkDirectoryItem(@NonNull BookmarkDirectory bookmarkDirectory) {
            super(bookmarkDirectory.getPath());

            mBookmarkDirectory = bookmarkDirectory;
        }

        @Override
        public long getId() {
            // We cannot simply use super.getId() because different bookmark directories may have
            // the same path.
            return mBookmarkDirectory.getId();
        }

        @DrawableRes
        @Override
        public int getIconRes() {
            return R.drawable.directory_icon_white_24dp;
        }

        @NonNull
        @Override
        public String getTitle(@NonNull Context context) {
            return mBookmarkDirectory.getName();
        }

        @Override
        protected int getTitleRes() {
            throw new AssertionError();
        }

        @Override
        public boolean onLongClick(@NonNull Listener listener) {
            listener.onEditBookmarkDirectory(mBookmarkDirectory);
            return true;
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
