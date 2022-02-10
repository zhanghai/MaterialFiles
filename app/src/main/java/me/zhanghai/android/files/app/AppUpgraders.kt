/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.app

import android.content.SharedPreferences
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.core.content.edit
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compat.PreferenceManagerCompat
import me.zhanghai.android.files.compat.getDescriptionCompat
import me.zhanghai.android.files.compat.writeBooleanCompat
import me.zhanghai.android.files.compat.writeParcelableListCompat
import me.zhanghai.android.files.file.DocumentTreeUri
import me.zhanghai.android.files.file.displayName
import me.zhanghai.android.files.file.storageVolume
import me.zhanghai.android.files.filelist.FileSortOptions
import me.zhanghai.android.files.navigation.BookmarkDirectory
import me.zhanghai.android.files.navigation.StandardDirectorySettings
import me.zhanghai.android.files.provider.archive.ArchiveFileSystem
import me.zhanghai.android.files.provider.common.ByteString
import me.zhanghai.android.files.provider.common.moveToByteString
import me.zhanghai.android.files.provider.content.ContentFileSystem
import me.zhanghai.android.files.provider.document.DocumentFileSystem
import me.zhanghai.android.files.provider.linux.LinuxFileSystem
import me.zhanghai.android.files.storage.DocumentTree
import me.zhanghai.android.files.storage.FileSystemRoot
import me.zhanghai.android.files.storage.PrimaryStorageVolume
import me.zhanghai.android.files.util.asBase64
import me.zhanghai.android.files.util.readParcelable
import me.zhanghai.android.files.util.toBase64
import me.zhanghai.android.files.util.toByteArray
import me.zhanghai.android.files.util.use

internal fun upgradeAppTo1_1_0() {
    // Migrate settings.
    migratePathSetting(R.string.pref_key_file_list_default_directory)
    migrateFileSortOptionsSetting()
    migrateCreateArchiveTypeSetting()
    migrateStandardDirectorySettingsSetting()
    migrateBookmarkDirectoriesSetting()
    migratePathSetting(R.string.pref_key_ftp_server_home_directory)
    for (key in pathSharedPreferences.all.keys) {
        migrateFileSortOptionsSetting(pathSharedPreferences, key)
    }
}

private const val PARCEL_VAL_PARCELABLE = 4
private const val PARCEL_VAL_LIST = 11

private fun migratePathSetting(@StringRes keyRes: Int) {
    val key = application.getString(keyRes)
    val oldBytes = defaultSharedPreferences.getString(key, null)?.asBase64()?.toByteArray()
        ?: return
    val newBytes = try {
        Parcel.obtain().use { newParcel ->
            newParcel.writeInt(PARCEL_VAL_PARCELABLE)
            Parcel.obtain().use { oldParcel ->
                oldParcel.unmarshall(oldBytes, 0, oldBytes.size)
                oldParcel.setDataPosition(0)
                migratePath(oldParcel, newParcel)
            }
            newParcel.marshall()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    defaultSharedPreferences.edit { putString(key, newBytes?.toBase64()?.value) }
}

private fun migrateFileSortOptionsSetting() {
    migrateFileSortOptionsSetting(
        defaultSharedPreferences, application.getString(R.string.pref_key_file_list_sort_options)
    )
}

private fun migrateFileSortOptionsSetting(sharedPreferences: SharedPreferences, key: String) {
    val oldBytes = sharedPreferences.getString(key, null)?.asBase64()?.toByteArray() ?: return
    val newBytes = try {
        Parcel.obtain().use { newParcel ->
            newParcel.writeInt(PARCEL_VAL_PARCELABLE)
            Parcel.obtain().use { oldParcel ->
                oldParcel.unmarshall(oldBytes, 0, oldBytes.size)
                oldParcel.setDataPosition(0)
                newParcel.writeString(oldParcel.readString())
                newParcel.writeString(FileSortOptions.By.values()[oldParcel.readInt()].name)
                newParcel.writeString(FileSortOptions.Order.values()[oldParcel.readInt()].name)
                newParcel.writeInt(oldParcel.readByte().toInt())
            }
            newParcel.marshall()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    sharedPreferences.edit { putString(key, newBytes?.toBase64()?.value) }
}

fun migrateCreateArchiveTypeSetting() {
    val key = application.getString(R.string.pref_key_create_archive_type)
    val oldValue = defaultSharedPreferences.getString(key, null) ?: return
    val newValue = oldValue.replace(Regex("type_.+$")) {
        when (it.value) {
            "type_zip" -> "zipRadio"
            "type_tar_xz" -> "tarXzRadio"
            "type_seven_z" -> "sevenZRadio"
            else -> "zipRadio"
        }
    }
    defaultSharedPreferences.edit { putString(key, newValue) }
}

private fun migrateStandardDirectorySettingsSetting() {
    val key = application.getString(R.string.pref_key_standard_directory_settings)
    val oldBytes = defaultSharedPreferences.getString(key, null)?.asBase64()?.toByteArray()
        ?: return
    val newBytes = try {
        Parcel.obtain().use { newParcel ->
            newParcel.writeInt(PARCEL_VAL_LIST)
            Parcel.obtain().use { oldParcel ->
                oldParcel.unmarshall(oldBytes, 0, oldBytes.size)
                oldParcel.setDataPosition(0)
                val size = oldParcel.readInt()
                newParcel.writeInt(size)
                repeat(size) {
                    oldParcel.readInt()
                    newParcel.writeInt(PARCEL_VAL_PARCELABLE)
                    newParcel.writeString(StandardDirectorySettings::class.java.name)
                    newParcel.writeString(oldParcel.readString())
                    newParcel.writeString(oldParcel.readString())
                    newParcel.writeInt(oldParcel.readByte().toInt())
                }
            }
            newParcel.marshall()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    defaultSharedPreferences.edit { putString(key, newBytes?.toBase64()?.value) }
}

private fun migrateBookmarkDirectoriesSetting() {
    val key = application.getString(R.string.pref_key_bookmark_directories)
    val oldBytes = defaultSharedPreferences.getString(key, null)?.asBase64()?.toByteArray()
        ?: return
    val newBytes = try {
        Parcel.obtain().use { newParcel ->
            newParcel.writeInt(PARCEL_VAL_LIST)
            Parcel.obtain().use { oldParcel ->
                oldParcel.unmarshall(oldBytes, 0, oldBytes.size)
                oldParcel.setDataPosition(0)
                val size = oldParcel.readInt()
                newParcel.writeInt(size)
                repeat(size) {
                    oldParcel.readInt()
                    newParcel.writeInt(PARCEL_VAL_PARCELABLE)
                    newParcel.writeString(BookmarkDirectory::class.java.name)
                    newParcel.writeLong(oldParcel.readLong())
                    newParcel.writeString(oldParcel.readString())
                    migratePath(oldParcel, newParcel)
                }
            }
            newParcel.marshall()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    defaultSharedPreferences.edit { putString(key, newBytes?.toBase64()?.value) }
}

private val oldByteStringCreator = object : Parcelable.Creator<ByteString> {
    override fun createFromParcel(source: Parcel): ByteString =
        source.createByteArray()!!.moveToByteString()

    override fun newArray(size: Int): Array<ByteString?> = arrayOfNulls(size)
}

private fun migratePath(oldParcel: Parcel, newParcel: Parcel) {
    val className = oldParcel.readString()
    newParcel.writeString(className)
    newParcel.writeByte(oldParcel.readByte())
    newParcel.writeBooleanCompat(oldParcel.readByte() != 0.toByte())
    newParcel.writeParcelableListCompat(oldParcel.createTypedArrayList(oldByteStringCreator), 0)
    when (className) {
        "me.zhanghai.android.files.provider.archive.ArchivePath" -> {
            oldParcel.readString()
            newParcel.writeString(ArchiveFileSystem::class.java.name)
            migratePath(oldParcel, newParcel)
        }
        "me.zhanghai.android.files.provider.content.ContentPath" -> {
            oldParcel.readString()
            newParcel.writeString(ContentFileSystem::class.java.name)
            newParcel.writeParcelable(oldParcel.readParcelable<Uri>(), 0)
        }
        "me.zhanghai.android.files.provider.document.DocumentPath" -> {
            oldParcel.readString()
            newParcel.writeString(DocumentFileSystem::class.java.name)
            newParcel.writeParcelable(oldParcel.readParcelable<Uri>(), 0)
        }
        "me.zhanghai.android.files.provider.linux.LinuxPath" -> {
            oldParcel.readString()
            newParcel.writeString(LinuxFileSystem::class.java.name)
            newParcel.writeBooleanCompat(oldParcel.readByte() != 0.toByte())
        }
        else -> throw IllegalStateException(className)
    }
}

private val pathSharedPreferences: SharedPreferences
    get() {
        val name = "${PreferenceManagerCompat.getDefaultSharedPreferencesName(application)}_path"
        val mode = PreferenceManagerCompat.defaultSharedPreferencesMode
        return application.getSharedPreferences(name, mode)
    }

internal fun upgradeAppTo1_2_0() {
    migrateStoragesSetting()
}

private fun migrateStoragesSetting() {
    val key = application.getString(R.string.pref_key_storages)
    val storages = (listOf(FileSystemRoot(null, true), PrimaryStorageVolume(null, true))
        + DocumentTreeUri.persistedUris.map {
            DocumentTree(
                null, it.storageVolume?.getDescriptionCompat(application) ?: it.displayName
                    ?: it.value.toString(), it
            )
        })
    val bytes = Parcel.obtain().use { parcel ->
        parcel.writeValue(storages)
        parcel.marshall()
    }
    defaultSharedPreferences.edit { putString(key, bytes.toBase64().value) }
}

internal fun upgradeAppTo1_3_0() {
    migrateSmbServersSetting()
}

private fun migrateSmbServersSetting() {
    val key = application.getString(R.string.pref_key_storages)
    val oldBytes = defaultSharedPreferences.getString(key, null)?.asBase64()?.toByteArray()
        ?: return
    val newBytes = try {
        Parcel.obtain().use { newParcel ->
            newParcel.writeInt(PARCEL_VAL_LIST)
            Parcel.obtain().use { oldParcel ->
                oldParcel.unmarshall(oldBytes, 0, oldBytes.size)
                oldParcel.setDataPosition(0)
                val size = oldParcel.readInt()
                newParcel.writeInt(size)
                repeat(size) {
                    val oldPosition = oldParcel.dataPosition()
                    oldParcel.readInt()
                    val className = oldParcel.readString()
                    if (className == "me.zhanghai.android.files.storage.SmbServer") {
                        newParcel.writeInt(PARCEL_VAL_PARCELABLE)
                        newParcel.writeString("me.zhanghai.android.files.storage.SmbServer")
                        val id = oldParcel.readLong()
                        newParcel.writeLong(id)
                        val customName = oldParcel.readString()
                        newParcel.writeString(customName)
                        oldParcel.readString()
                        newParcel.writeString(
                            "me.zhanghai.android.files.provider.smb.client.Authority"
                        )
                        val authorityHost = oldParcel.readString()
                        newParcel.writeString(authorityHost)
                        val authorityPort = oldParcel.readInt()
                        newParcel.writeInt(authorityPort)
                        oldParcel.readString()
                        newParcel.writeString(
                            "me.zhanghai.android.files.provider.smb.client.Authentication"
                        )
                        val authenticationUsername = oldParcel.readString()
                        newParcel.writeString(authenticationUsername)
                        val authenticationDomain = oldParcel.readString()
                        newParcel.writeString(authenticationDomain)
                        val authenticationPassword = oldParcel.readString()
                        newParcel.writeString(authenticationPassword)
                        val relativePath = ""
                        newParcel.writeString(relativePath)
                    } else {
                        oldParcel.setDataPosition(oldPosition)
                        val storage = oldParcel.readValue(appClassLoader)
                        newParcel.writeValue(storage)
                    }
                }
            }
            newParcel.marshall()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    defaultSharedPreferences.edit { putString(key, newBytes?.toBase64()?.value) }
}

internal fun upgradeAppTo1_4_0() {
    migrateSftpAndSmbServersSetting()
}

private fun migrateSftpAndSmbServersSetting() {
    val key = application.getString(R.string.pref_key_storages)
    val oldBytes = defaultSharedPreferences.getString(key, null)?.asBase64()?.toByteArray()
        ?: return
    val newBytes = try {
        Parcel.obtain().use { newParcel ->
            Parcel.obtain().use { oldParcel ->
                oldParcel.unmarshall(oldBytes, 0, oldBytes.size)
                oldParcel.setDataPosition(0)
                oldParcel.readInt()
                newParcel.writeInt(PARCEL_VAL_LIST)
                val size = oldParcel.readInt()
                newParcel.writeInt(size)
                repeat(size) {
                    val oldPosition = oldParcel.dataPosition()
                    oldParcel.readInt()
                    when (oldParcel.readString()) {
                        "me.zhanghai.android.files.storage.SftpServer" -> {
                            newParcel.writeInt(PARCEL_VAL_PARCELABLE)
                            newParcel.writeString("me.zhanghai.android.files.storage.SftpServer")
                            val id = oldParcel.readLong()
                            newParcel.writeLong(id)
                            val customName = oldParcel.readString()
                            newParcel.writeString(customName)
                            oldParcel.readString()
                            newParcel.writeString(
                                "me.zhanghai.android.files.provider.sftp.client.Authority"
                            )
                            val authorityHost = oldParcel.readString()
                            newParcel.writeString(authorityHost)
                            val authorityPort = oldParcel.readInt()
                            newParcel.writeInt(authorityPort)
                            val authenticationClassName = oldParcel.readString()
                            val authorityUsername = oldParcel.readString()
                            newParcel.writeString(authorityUsername)
                            newParcel.writeString(authenticationClassName)
                            val authenticationPasswordOrPrivateKey = oldParcel.readString()
                            newParcel.writeString(authenticationPasswordOrPrivateKey)
                            val relativePath = oldParcel.readString()
                            newParcel.writeString(relativePath)
                        }
                        "me.zhanghai.android.files.storage.SmbServer" -> {
                            newParcel.writeInt(PARCEL_VAL_PARCELABLE)
                            newParcel.writeString("me.zhanghai.android.files.storage.SmbServer")
                            val id = oldParcel.readLong()
                            newParcel.writeLong(id)
                            val customName = oldParcel.readString()
                            newParcel.writeString(customName)
                            oldParcel.readString()
                            newParcel.writeString(
                                "me.zhanghai.android.files.provider.smb.client.Authority"
                            )
                            val authorityHost = oldParcel.readString()
                            newParcel.writeString(authorityHost)
                            val authorityPort = oldParcel.readInt()
                            newParcel.writeInt(authorityPort)
                            oldParcel.readString()
                            val authorityUsername = oldParcel.readString()
                            newParcel.writeString(authorityUsername)
                            val authorityDomain = oldParcel.readString()
                            newParcel.writeString(authorityDomain)
                            val password = oldParcel.readString()
                            newParcel.writeString(password)
                            val relativePath = oldParcel.readString()
                            newParcel.writeString(relativePath)
                        }
                        else -> {
                            oldParcel.setDataPosition(oldPosition)
                            val storage = oldParcel.readValue(appClassLoader)
                            newParcel.writeValue(storage)
                        }
                    }
                }
            }
            newParcel.marshall()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    defaultSharedPreferences.edit { putString(key, newBytes?.toBase64()?.value) }
}
