/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.apk

import android.content.pm.PackageManager
import android.os.AsyncTask
import java8.nio.file.Path
import me.zhanghai.android.files.app.packageManager
import me.zhanghai.android.files.fileproperties.PathObserverLiveData
import me.zhanghai.android.files.util.Failure
import me.zhanghai.android.files.util.Loading
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.Success
import me.zhanghai.android.files.util.sha1Digest
import me.zhanghai.android.files.util.toHexString
import java.io.IOException

class ApkInfoLiveData(path: Path) : PathObserverLiveData<Stateful<ApkInfo>>(path) {
    init {
        loadValue()
        observe()
    }

    override fun loadValue() {
        value = Loading()
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            val value = try {
                val apkPath = path.toFile().path
                val packageInfo = packageManager.getPackageArchiveInfo(
                    apkPath, PackageManager.GET_SIGNATURES
                ) ?: throw IOException("PackageManager.getPackageArchiveInfo() returned null")
                val applicationInfo = packageInfo.applicationInfo
                    ?: throw IOException("PackageInfo.applicationInfo is null")
                applicationInfo.sourceDir = apkPath
                applicationInfo.publicSourceDir = apkPath
                val label = applicationInfo.loadLabel(packageManager).toString()
                val signatures = packageInfo.signatures.map {
                    it.toByteArray().sha1Digest().toHexString()
                }
                Success(ApkInfo(packageInfo, label, signatures))
            } catch (e: Exception) {
                Failure(e)
            }
            postValue(value)
        }
    }
}
