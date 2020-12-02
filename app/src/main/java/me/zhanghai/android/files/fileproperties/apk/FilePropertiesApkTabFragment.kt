/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.apk

import android.os.Build
import android.os.Bundle
import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import me.zhanghai.android.files.R
import me.zhanghai.android.files.app.packageManager
import me.zhanghai.android.files.compat.longVersionCodeCompat
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.file.isApk
import me.zhanghai.android.files.fileproperties.FilePropertiesTabFragment
import me.zhanghai.android.files.provider.linux.isLinuxPath
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.ParcelableParceler
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.getStringArray
import me.zhanghai.android.files.util.viewModels
import java.text.Collator


class FilePropertiesApkTabFragment : FilePropertiesTabFragment() {
    private val args by args<Args>()

    private val viewModel by viewModels { { FilePropertiesApkTabViewModel(args.path) } }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.apkInfoLiveData.observe(viewLifecycleOwner) { onApkInfoChanged(it) }
    }

    override fun refresh() {
        viewModel.reload()
    }

    private fun onApkInfoChanged(stateful: Stateful<ApkInfo>) {
        bindView(stateful) { apkInfo ->
            addItemView(R.string.file_properties_apk_label, apkInfo.label)
            val packageInfo = apkInfo.packageInfo
            addItemView(R.string.file_properties_apk_package_name, packageInfo.packageName)
            addItemView(
                R.string.file_properties_apk_version, getString(
                    R.string.file_properties_apk_version_format, packageInfo.versionName,
                    packageInfo.longVersionCodeCompat
                )
            )
            val applicationInfo = packageInfo.applicationInfo
            // PackageParser didn't return minSdkVersion before N, so it's hard to implement a
            // compat version.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                addItemView(
                    R.string.file_properties_apk_min_sdk_version,
                    getSdkVersionText(applicationInfo.minSdkVersion)
                )
            }
            addItemView(
                R.string.file_properties_apk_target_sdk_version,
                getSdkVersionText(applicationInfo.targetSdkVersion)
            )
            addItemView(
                R.string.file_properties_apk_signature_digests,
                apkInfo.signingCertificateDigests.joinToString("\n")
            )
            if (apkInfo.pastSigningCertificateDigests.isNotEmpty()) {
                addItemView(
                    R.string.file_properties_apk_past_signature_digests,
                    apkInfo.pastSigningCertificateDigests.joinToString("\n")
                )
            }
            val requestedPermissions = packageInfo.requestedPermissions
            if (!requestedPermissions.isNullOrEmpty()) {
                val localizedPermissions = mutableListOf<String>()
                val rawPermissions = mutableListOf<String>()
                requestedPermissions.forEach {
                    val label: String? = try {
                        val permissionInfo = packageManager.getPermissionInfo(it, 0)
                        permissionInfo.loadLabel(packageManager).toString()
                    } catch (e: Throwable) {
                        null
                    }
                    if (label.isNullOrEmpty()) rawPermissions.add(it) else localizedPermissions.add(label)
                }
                localizedPermissions.sortWith { a, b -> Collator.getInstance().compare(a, b) }
                rawPermissions.sort()
                addItemView(
                    R.string.file_properties_apk_permissions,
                    (localizedPermissions + rawPermissions).joinToString(System.lineSeparator())
                )
            }
        }
    }

    private fun getSdkVersionText(sdkVersion: Int): String {
        val names = getStringArray(R.array.file_properites_apk_sdk_version_names)
        val codeNames = getStringArray(R.array.file_properites_apk_sdk_version_codenames)
        return getString(
            R.string.file_properites_apk_sdk_version_format,
            names[sdkVersion.coerceIn(names.indices)],
            codeNames[sdkVersion.coerceIn(codeNames.indices)], sdkVersion
        )
    }

    companion object {
        fun isAvailable(file: FileItem): Boolean = file.mimeType.isApk && file.path.isLinuxPath
    }

    @Parcelize
    class Args(val path: @WriteWith<ParcelableParceler> Path) : ParcelableArgs
}
