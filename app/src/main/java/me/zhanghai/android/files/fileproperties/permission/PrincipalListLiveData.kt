/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permission

import android.content.pm.ApplicationInfo
import android.os.AsyncTask
import android.os.Process
import androidx.lifecycle.MutableLiveData
import com.topjohnwu.superuser.Shell
import java.util.regex.Pattern
import me.zhanghai.android.files.app.packageManager
import me.zhanghai.android.files.provider.root.LibSuFileServiceLauncher
import me.zhanghai.android.files.provider.root.RootStrategy
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.Failure
import me.zhanghai.android.files.util.Loading
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.Success
import me.zhanghai.android.files.util.valueCompat

abstract class PrincipalListLiveData : MutableLiveData<Stateful<List<PrincipalItem>>>() {
    init {
        loadValue()
    }

    private fun loadValue() {
        value = Loading(value?.value)
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            val value = try {
                val principals = androidPrincipals
                val androidIds = principals.mapTo(mutableSetOf()) { it.id }
                val installedApplicationInfos = packageManager.getInstalledApplications(0)
                val uidApplicationInfoMap = mutableMapOf<Int, MutableList<ApplicationInfo>>()
                for (applicationInfo in installedApplicationInfos) {
                    val uid = applicationInfo.uid
                    if (uid in androidIds) {
                        continue
                    }
                    uidApplicationInfoMap.getOrPut(uid) { mutableListOf() }.add(applicationInfo)
                }
                for ((uid, applicationInfos) in uidApplicationInfoMap) {
                    val principal = PrincipalItem(
                        uid, getAppPrincipalName(uid), applicationInfos,
                        applicationInfos.map { it.loadLabel(packageManager).toString() }
                    )
                    principals.add(principal)
                }
                if (Settings.ROOT_STRATEGY.valueCompat != RootStrategy.NEVER) {
                    try {
                        principals.addAll(enumerateOtherUsersPrincipals(androidIds))
                    } catch (e: Exception) {
                        // Root isn't available or the query failed, just ignore.
                        e.printStackTrace()
                    }
                }
                principals.sortBy { it.id }
                Success(principals as List<PrincipalItem>)
            } catch (e: Exception) {
                Failure(valueCompat.value, e)
            }
            postValue(value)
        }
    }

    @get:Throws(Exception::class)
    protected abstract val androidPrincipals: MutableList<PrincipalItem>

    internal abstract fun getAppPrincipalName(uid: Int): String

    @Throws(Exception::class)
    private fun enumerateOtherUsersPrincipals(androidIds: MutableSet<Int>): List<PrincipalItem> {
        // Also makes sure the default shell builder has been initialized.
        if (!LibSuFileServiceLauncher.isSuAvailable()) {
            return emptyList()
        }
        val userIds = mutableListOf<Int>()
        val usersResult = Shell.cmd("pm list users").exec()
        if (usersResult.isSuccess) {
            for (line in usersResult.out) {
                val matcher = USER_ID_PATTERN.matcher(line)
                if (matcher.find()) {
                    userIds.add(matcher.group(1)!!.toInt())
                }
            }
        }
        val currentUserId = Process.myUid() / AID_USER_OFFSET
        // Lines are of the form "package:<apkPath>=<packageName> uid:<uid>".
        val uidPackagesMap = mutableMapOf<Int, MutableMap<String, String?>>()
        for (userId in userIds) {
            if (userId == currentUserId) {
                continue
            }
            val packagesResult = Shell.cmd("pm list packages --user $userId -U -f").exec()
            if (!packagesResult.isSuccess) {
                continue
            }
            for (line in packagesResult.out) {
                if (!line.startsWith("package:")) {
                    continue
                }
                val nameAndPathAndUid = line.substring("package:".length)
                val separatorIndex = nameAndPathAndUid.lastIndexOf(' ')
                if (separatorIndex == -1 ||
                    !nameAndPathAndUid.startsWith("uid:", separatorIndex + 1)) {
                    continue
                }
                val uid = nameAndPathAndUid.substring(separatorIndex + 1 + "uid:".length)
                    .toIntOrNull() ?: continue
                val nameAndPath = nameAndPathAndUid.substring(0, separatorIndex)
                // Split at the last '=' because APK paths under /data/app end with base64
                // suffixes that contain '=' themselves, e.g. "/data/app/~~abc==/pkg-x==/base.apk".
                val equalsIndex = nameAndPath.lastIndexOf('=')
                if (equalsIndex == -1) {
                    continue
                }
                val apkPath = nameAndPath.substring(0, equalsIndex).ifEmpty { null }
                val packageName = nameAndPath.substring(equalsIndex + 1)
                uidPackagesMap.getOrPut(uid) { linkedMapOf() }[packageName] = apkPath
            }
        }
        val principals = mutableListOf<PrincipalItem>()
        for ((uid, packages) in uidPackagesMap) {
            if (uid in androidIds) {
                continue
            }
            androidIds.add(uid)
            val applicationInfos = mutableListOf<ApplicationInfo>()
            val packageNames = mutableListOf<String>()
            for ((packageName, apkPath) in packages) {
                packageNames.add(packageName)
                // The APK itself is world-readable, so parsing it as an archive yields the same
                // icon and label rendering as applications of the current user.
                val applicationInfo = apkPath?.let {
                    try {
                        packageManager.getPackageArchiveInfo(it, 0)?.applicationInfo
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                } ?: continue
                applicationInfo.uid = uid
                applicationInfos.add(applicationInfo)
            }
            val applicationLabels = applicationInfos.mapTo(mutableListOf()) {
                try {
                    it.loadLabel(packageManager).toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                    it.packageName ?: ""
                }
            }.ifEmpty { packageNames }
            principals.add(
                PrincipalItem(uid, getAppPrincipalName(uid), applicationInfos, applicationLabels)
            )
        }
        return principals
    }

    companion object {
        private val USER_ID_PATTERN = Pattern.compile("UserInfo\\{(\\d+)")

        @JvmStatic
        protected val AID_USER_OFFSET = 100000
        @JvmStatic
        protected val AID_APP_START = 10000
        @JvmStatic
        protected val AID_CACHE_GID_START = 20000
        @JvmStatic
        protected val AID_CACHE_GID_END = 29999
        @JvmStatic
        protected val AID_SHARED_GID_START = 50000
        @JvmStatic
        protected val AID_SHARED_GID_END = 59999
        @JvmStatic
        protected val AID_ISOLATED_START = 99000
    }
}
