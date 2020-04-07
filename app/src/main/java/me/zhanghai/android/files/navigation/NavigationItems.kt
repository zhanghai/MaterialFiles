/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.storage.StorageVolume
import android.provider.DocumentsContract
import androidx.annotation.DrawableRes
import androidx.annotation.Size
import androidx.annotation.StringRes
import java8.nio.file.Path
import java8.nio.file.Paths
import me.zhanghai.android.files.R
import me.zhanghai.android.files.about.AboutActivity
import me.zhanghai.android.files.app.storageManager
import me.zhanghai.android.files.compat.DocumentsContractCompat
import me.zhanghai.android.files.compat.createOpenDocumentTreeIntentCompat
import me.zhanghai.android.files.compat.getDescriptionCompat
import me.zhanghai.android.files.compat.isPrimaryCompat
import me.zhanghai.android.files.compat.pathCompat
import me.zhanghai.android.files.compat.storageVolumesCompat
import me.zhanghai.android.files.file.DocumentTreeUri
import me.zhanghai.android.files.file.JavaFile
import me.zhanghai.android.files.file.asDocumentTreeUri
import me.zhanghai.android.files.file.asFileSize
import me.zhanghai.android.files.file.displayNameOrUri
import me.zhanghai.android.files.ftpserver.FtpServerActivity
import me.zhanghai.android.files.provider.document.createDocumentTreeRootPath
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.settings.SettingsActivity
import me.zhanghai.android.files.util.createIntent
import me.zhanghai.android.files.util.getParcelableExtraSafe
import me.zhanghai.android.files.util.valueCompat
import java.util.Objects

val navigationItems: List<NavigationItem?>
    get() =
        mutableListOf<NavigationItem?>().apply {
            addAll(rootItems)
            val standardDirectoryItems = standardDirectoryItems
            if (standardDirectoryItems.isNotEmpty()) {
                add(null)
                addAll(standardDirectoryItems)
            }
            val bookmarkDirectoryItems = bookmarkDirectoryItems
            if (bookmarkDirectoryItems.isNotEmpty()) {
                add(null)
                addAll(bookmarkDirectoryItems)
            }
            add(null)
            addAll(menuItems)
        }

private val rootItems: List<NavigationItem>
    @Size(min = 1)
    get() =
        mutableListOf<NavigationItem>().apply {
            add(RootDirectoryRootItem())
            val storageVolumes = storageManager.storageVolumesCompat
            for (storageVolume in storageVolumes) {
                if (storageVolume.isPrimaryCompat) {
                    add(PrimaryStorageVolumeRootItem(storageVolume))
                }
            }
            val treeUris = DocumentTreesLiveData.valueCompat.toMutableList()
            for (storageVolume in storageVolumes) {
                if (storageVolume.isPrimaryCompat) {
                    continue
                }
                val treeUri = getStorageVolumeTreeUri(storageVolume)
                if (treeUris.remove(treeUri)) {
                    add(DocumentTreeStorageVolumeRootItem(treeUri, storageVolume))
                }
            }
            for (treeUri in treeUris) {
                add(DocumentTreeRootItem(treeUri))
            }
            add(AddDocumentTreeItem())
        }

private abstract class FileItem(val path: Path) : NavigationItem() {
    // Items of different types may point to the same file.
    override val id: Long = Objects.hash(javaClass, path).toLong()

    override fun isChecked(listener: Listener): Boolean = listener.currentPath == path

    override fun onClick(listener: Listener) {
        listener.navigateTo(path)
        listener.closeNavigationDrawer()
    }
}

private abstract class RootItem(path: Path) : FileItem(path), NavigationRoot {
    override fun onClick(listener: Listener) {
        listener.navigateToRoot(path)
        listener.closeNavigationDrawer()
    }

    override fun getName(context: Context): String = getTitle(context)
}

private const val PATH_ROOT = "/"

private abstract class LocalRootItem(
    path: Path,
    pathString: String,
    @DrawableRes override val iconRes: Int
) : RootItem(path) {
    private var freeSpace: Long
    private var totalSpace: Long

    constructor(path: String, @DrawableRes iconRes: Int) : this(Paths.get(path), path, iconRes)

    init {
        val totalSpace = JavaFile.getTotalSpace(pathString)
        when {
            totalSpace != 0L -> {
                freeSpace = JavaFile.getFreeSpace(pathString)
                this.totalSpace = totalSpace
            }
            pathString == PATH_ROOT -> {
                // Root directory may not be an actual partition on legacy Android versions (can be
                // a ramdisk instead). On modern Android the system partition will be mounted as
                // root instead so let's try with the system partition again.
                // @see https://source.android.com/devices/bootloader/system-as-root
                val systemPath = Environment.getRootDirectory().path
                freeSpace = JavaFile.getFreeSpace(systemPath)
                this.totalSpace = JavaFile.getTotalSpace(systemPath)
            }
            else -> {
                freeSpace = 0
                this.totalSpace = 0
            }
        }
    }

    override fun getSubtitle(context: Context): String? {
        if (totalSpace == 0L) {
            return null
        }
        val freeSpace = freeSpace.asFileSize().formatHumanReadable(context)
        val totalSpace = totalSpace.asFileSize().formatHumanReadable(context)
        return context.getString(R.string.navigation_root_subtitle_format, freeSpace, totalSpace)
    }
}

private class RootDirectoryRootItem : LocalRootItem(PATH_ROOT, R.drawable.device_icon_white_24dp) {
    @StringRes
    override val titleRes: Int? = R.string.navigation_root_directory
}

private class PrimaryStorageVolumeRootItem(
    private val storageVolume: StorageVolume
) : LocalRootItem(storageVolume.pathCompat, R.drawable.sd_card_icon_white_24dp) {
    override fun getTitle(context: Context): String = storageVolume.getDescriptionCompat(context)

    @StringRes
    override val titleRes: Int? = null
}

private class DocumentTreeStorageVolumeRootItem(
    private val treeUri: DocumentTreeUri,
    private val storageVolume: StorageVolume
) : LocalRootItem(
    treeUri.value.createDocumentTreeRootPath(), storageVolume.pathCompat,
    R.drawable.sd_card_icon_white_24dp
) {
    override fun getTitle(context: Context): String = storageVolume.getDescriptionCompat(context)

    @StringRes
    override val titleRes: Int? = null

    override fun onLongClick(listener: Listener): Boolean {
        listener.onRemoveDocumentTree(treeUri, storageVolume)
        return true
    }
}

private class DocumentTreeRootItem(private val treeUri: DocumentTreeUri) : RootItem(
    treeUri.value.createDocumentTreeRootPath()
) {
    private val title: String = treeUri.displayNameOrUri

    @DrawableRes
    override val iconRes: Int? = R.drawable.directory_icon_white_24dp

    override fun getTitle(context: Context): String = title

    @StringRes
    override val titleRes: Int? = null

    override fun onLongClick(listener: Listener): Boolean {
        listener.onRemoveDocumentTree(treeUri, null)
        return true
    }
}

private class AddDocumentTreeItem : NavigationItem() {
    override val id: Long = R.string.navigation_add_document_tree.toLong()

    @DrawableRes
    override val iconRes: Int? = R.drawable.add_icon_white_24dp

    @StringRes
    override val titleRes: Int? = R.string.navigation_add_document_tree

    override fun onClick(listener: Listener) {
        listener.onAddDocumentTree()
    }
}

private fun getStorageVolumeTreeUri(storageVolume: StorageVolume): DocumentTreeUri {
    val intent = storageVolume.createOpenDocumentTreeIntentCompat()
    val rootUri = intent.getParcelableExtraSafe<Uri>(DocumentsContractCompat.EXTRA_INITIAL_URI)!!
    // @see com.android.externalstorage.ExternalStorageProvider#getDocIdForFile(File)
    // @see com.android.documentsui.picker.ConfirmFragment#onCreateDialog(Bundle)
    return DocumentsContract.buildTreeDocumentUri(
        rootUri.authority,
        DocumentsContract.getRootId(rootUri) + ":"
    ).asDocumentTreeUri()
}

private val standardDirectoryItems: List<NavigationItem>
    @Size(min = 0)
    get() =
        StandardDirectoriesLiveData.valueCompat
            .filter { it.isEnabled }
            .map { StandardDirectoryItem(it) }

private class StandardDirectoryItem(private val standardDirectory: StandardDirectory) : FileItem(
    Paths.get(getExternalStorageDirectory(standardDirectory.relativePath))
) {
    init {
        require(standardDirectory.isEnabled) { "StandardDirectory should be enabled" }
    }

    @DrawableRes
    override val iconRes: Int? = standardDirectory.iconRes

    override fun getTitle(context: Context): String = standardDirectory.getTitle(context)

    @StringRes
    override val titleRes: Int? = null
}

val standardDirectories: List<StandardDirectory>
    get() {
        val settingsMap = Settings.STANDARD_DIRECTORY_SETTINGS.valueCompat.associateBy { it.id }
        return defaultStandardDirectories.map {
            val settings = settingsMap[it.id]
            if (settings != null) it.withSettings(settings) else it
        }
    }

private val defaultStandardDirectories: List<StandardDirectory>
    // HACK: Show QQ, TIM and WeChat standard directories based on whether the directory exists.
    get() =
        DEFAULT_STANDARD_DIRECTORIES.filter {
            when (it.iconRes) {
                R.drawable.qq_icon_white_24dp, R.drawable.tim_icon_white_24dp,
                R.drawable.wechat_icon_white_24dp -> {
                    val path = getExternalStorageDirectory(it.relativePath)
                    JavaFile.isDirectory(path)
                }
                else -> true
            }
        }

// @see android.os.Environment#STANDARD_DIRECTORIES
private val DEFAULT_STANDARD_DIRECTORIES = listOf(
    StandardDirectory(
        R.drawable.alarm_icon_white_24dp, R.string.navigation_standard_directory_alarms,
        Environment.DIRECTORY_ALARMS, false
    ),
    StandardDirectory(
        R.drawable.camera_icon_white_24dp, R.string.navigation_standard_directory_dcim,
        Environment.DIRECTORY_DCIM, true
    ),
    StandardDirectory(
        R.drawable.document_icon_white_24dp, R.string.navigation_standard_directory_documents,
        Environment.DIRECTORY_DOCUMENTS, false),
    StandardDirectory(
        R.drawable.download_icon_white_24dp, R.string.navigation_standard_directory_downloads,
        Environment.DIRECTORY_DOWNLOADS, true
    ),
    StandardDirectory(
        R.drawable.video_icon_white_24dp, R.string.navigation_standard_directory_movies,
        Environment.DIRECTORY_MOVIES, true
    ),
    StandardDirectory(
        R.drawable.audio_icon_white_24dp, R.string.navigation_standard_directory_music,
        Environment.DIRECTORY_MUSIC, true
    ),
    StandardDirectory(
        R.drawable.notification_icon_white_24dp,
        R.string.navigation_standard_directory_notifications, Environment.DIRECTORY_NOTIFICATIONS,
        false
    ),
    StandardDirectory(
        R.drawable.image_icon_white_24dp, R.string.navigation_standard_directory_pictures,
        Environment.DIRECTORY_PICTURES, true
    ),
    StandardDirectory(
        R.drawable.podcast_icon_white_24dp, R.string.navigation_standard_directory_podcasts,
        Environment.DIRECTORY_PODCASTS, false
    ),
    StandardDirectory(
        R.drawable.ringtone_icon_white_24dp, R.string.navigation_standard_directory_ringtones,
        Environment.DIRECTORY_RINGTONES, false
    ),
    StandardDirectory(
        R.drawable.qq_icon_white_24dp, R.string.navigation_standard_directory_qq,
        "tencent/QQfile_recv", true
    ),
    StandardDirectory(
        R.drawable.tim_icon_white_24dp, R.string.navigation_standard_directory_tim,
        "tencent/TIMfile_recv", true
    ),
    StandardDirectory(
        R.drawable.wechat_icon_white_24dp, R.string.navigation_standard_directory_wechat,
        "tencent/MicroMsg/Download", true
    )
)

internal fun getExternalStorageDirectory(relativePath: String): String =
    @Suppress("DEPRECATION")
    Environment.getExternalStoragePublicDirectory(relativePath).path

private val bookmarkDirectoryItems: List<NavigationItem>
    @Size(min = 0)
    get() = Settings.BOOKMARK_DIRECTORIES.valueCompat.map { BookmarkDirectoryItem(it) }

private class BookmarkDirectoryItem(private val bookmarkDirectory: BookmarkDirectory) : FileItem(
    bookmarkDirectory.path
) {
    // We cannot simply use super.getId() because different bookmark directories may have
    // the same path.
    override val id: Long = bookmarkDirectory.id

    @DrawableRes
    override val iconRes: Int? = R.drawable.directory_icon_white_24dp

    override fun getTitle(context: Context): String = bookmarkDirectory.name

    @StringRes
    override val titleRes: Int? = null

    override fun onLongClick(listener: Listener): Boolean {
        listener.onEditBookmarkDirectory(bookmarkDirectory)
        return true
    }
}

private val menuItems: List<NavigationItem>
    @Size(3)
    get() = listOf(
        ActivityMenuItem(
            R.drawable.shared_directory_icon_white_24dp, R.string.navigation_ftp_server,
            FtpServerActivity::class.createIntent()
        ),
        ActivityMenuItem(
            R.drawable.settings_icon_white_24dp, R.string.navigation_settings,
            SettingsActivity::class.createIntent()
        ),
        ActivityMenuItem(
            R.drawable.about_icon_white_24dp, R.string.navigation_about,
            AboutActivity::class.createIntent()
        )
    )

private abstract class MenuItem(
    @DrawableRes override val iconRes: Int,
    @StringRes override val titleRes: Int
) : NavigationItem()

private class ActivityMenuItem(
    @DrawableRes iconRes: Int,
    @StringRes titleRes: Int,
    private val intent: Intent
) : MenuItem(iconRes, titleRes) {
    override val id: Long
        get() = intent.component.hashCode().toLong()

    override fun onClick(listener: Listener) {
        // TODO: startActivitySafe()?
        listener.startActivity(intent)
        listener.closeNavigationDrawer()
    }
}
