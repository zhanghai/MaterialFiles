/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.storage.StorageVolume
import androidx.lifecycle.LiveData
import me.zhanghai.android.files.app.application
import me.zhanghai.android.files.app.storageManager
import me.zhanghai.android.files.compat.storageVolumesCompat

object StorageVolumeListLiveData : LiveData<List<StorageVolume>>() {
    init {
        loadValue()
        application.registerReceiver(
            object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    loadValue()
                }
            }, IntentFilter().apply {
                // @see android.os.storage.VolumeInfo#sEnvironmentToBroadcast
                addAction(Intent.ACTION_MEDIA_UNMOUNTED)
                addAction(Intent.ACTION_MEDIA_CHECKING)
                addAction(Intent.ACTION_MEDIA_MOUNTED)
                addAction(Intent.ACTION_MEDIA_EJECT)
                addAction(Intent.ACTION_MEDIA_UNMOUNTABLE)
                addAction(Intent.ACTION_MEDIA_REMOVED)
                addAction(Intent.ACTION_MEDIA_BAD_REMOVAL)
                // The "file" data scheme is required to receive these broadcasts.
                // @see https://stackoverflow.com/a/7143298
                addDataScheme(ContentResolver.SCHEME_FILE)
            }
        )
    }

    private fun loadValue() {
        value = storageManager.storageVolumesCompat
    }
}
