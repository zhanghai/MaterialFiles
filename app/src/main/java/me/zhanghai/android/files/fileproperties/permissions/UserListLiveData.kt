/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions

import me.zhanghai.android.files.provider.linux.syscall.Syscall.endpwent
import me.zhanghai.android.files.provider.linux.syscall.Syscall.getpwent
import me.zhanghai.android.files.provider.linux.syscall.Syscall.setpwent

class UserListLiveData : PrincipalListLiveData() {
    override val androidPrincipals: MutableList<PrincipalItem>
        @Throws(Exception::class)
        get() {
            val users = mutableListOf<PrincipalItem>()
            setpwent()
            try {
                while (true) {
                    val passwd = getpwent() ?: break
                    val user = PrincipalItem(
                        passwd.pw_uid, passwd.pw_name?.toString(), emptyList(), emptyList()
                    )
                    users.add(user)
                }
            } finally {
                endpwent()
            }
            return users
        }

    /*
     * @see https://android.googlesource.com/platform/bionic/+/android10-release/libc/bionic/grp_pwd.cpp
     *      print_app_name_from_uid()
     */
    override fun getAppPrincipalName(uid: Int): String {
        val userId = uid / AID_USER_OFFSET
        val appId = uid % AID_USER_OFFSET
        return when {
            appId > AID_ISOLATED_START -> "u${userId}_i${appId - AID_ISOLATED_START}"
            else -> "u${userId}_a${appId - AID_APP_START}"
        }
    }
}
