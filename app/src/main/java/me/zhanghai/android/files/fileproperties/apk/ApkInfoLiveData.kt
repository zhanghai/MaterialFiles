/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.apk

import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import java8.nio.file.Path
import me.zhanghai.android.files.app.packageManager
import me.zhanghai.android.files.compat.getPackageArchiveInfoCompat
import me.zhanghai.android.files.fileproperties.PathObserverLiveData
import me.zhanghai.android.files.util.Failure
import me.zhanghai.android.files.util.Loading
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.Success
import me.zhanghai.android.files.util.sha1Digest
import me.zhanghai.android.files.util.toHexString
import me.zhanghai.android.files.util.valueCompat
import java.io.IOException

class ApkInfoLiveData(path: Path) : PathObserverLiveData<Stateful<ApkInfo>>(path) {
    init {
        loadValue()
        observe()
    }

    override fun loadValue() {
        value = Loading(value?.value)
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            val value = try {
                val apkPath = path.toFile().path
                // We must always pass in PackageManager.GET_SIGNATURES for
                // PackageManager.getPackageArchiveInfo() to call
                // PackageParser.collectCertificates().
                @Suppress("DEPRECATION")
                var packageInfoFlags = (PackageManager.GET_PERMISSIONS
                    or PackageManager.GET_SIGNATURES)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfoFlags = packageInfoFlags or PackageManager.GET_SIGNING_CERTIFICATES
                }
                val packageInfo =
                    packageManager.getPackageArchiveInfoCompat(apkPath, packageInfoFlags)
                        ?: throw IOException("PackageManager.getPackageArchiveInfo() returned null")
                val applicationInfo = packageInfo.applicationInfo
                    ?: throw IOException("PackageInfo.applicationInfo is null")
                applicationInfo.sourceDir = apkPath
                applicationInfo.publicSourceDir = apkPath
                val label = applicationInfo.loadLabel(packageManager).toString()
                val signingCertificates = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    // PackageInfo.signatures returns only the oldest certificate if there are past
                    // certificates on P and above for compatibility.
                    packageInfo.signingInfo.apkContentsSigners ?: emptyArray()
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.signatures
                }
                val signingCertificateDigests = signingCertificates
                    .map { it.toByteArray().sha1Digest().toHexString() }
                val pastSigningCertificates = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val signingInfo = packageInfo.signingInfo
                    // SigningInfo.getSigningCertificateHistory() may return the current certificate
                    // if there are no past certificates.
                    if (signingInfo.hasPastSigningCertificates()) {
                        // SigningInfo.getSigningCertificateHistory() also returns the current
                        // certificate.
                        signingInfo.signingCertificateHistory?.toMutableList()
                            ?.apply { removeAll(signingCertificates) }
                    } else {
                        null
                    }
                } else {
                    null
                } ?: emptyList()
                val pastSigningCertificateDigests = pastSigningCertificates
                    .map { it.toByteArray().sha1Digest().toHexString() }
                val apkInfo = ApkInfo(
                    packageInfo, label, signingCertificateDigests, pastSigningCertificateDigests
                )
                Success(apkInfo)
            } catch (e: Exception) {
                Failure(valueCompat.value, e)
            }
            postValue(value)
        }
    }
}
