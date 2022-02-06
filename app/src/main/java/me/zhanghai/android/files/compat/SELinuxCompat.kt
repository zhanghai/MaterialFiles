/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.os.Build
import androidx.annotation.RequiresApi
import me.zhanghai.android.files.hiddenapi.RestrictedHiddenApi
import me.zhanghai.android.files.util.lazyReflectedClass
import me.zhanghai.android.files.util.lazyReflectedMethod
import java.io.File
import java.io.FileDescriptor

/*
 * @see android.os.SELinux
 * @see <a href="https://android.googlesource.com/platform/frameworks/base/+/jb-mr1-release/core/java/android/os/SELinux.java">
 *      jb-mr1-release/SELinux.java</a>
 * @see <a href="https://android.googlesource.com/platform/prebuilts/runtime/+/master/appcompat/hiddenapi-light-greylist.txt">
 *      hiddenapi-light-greylist.txt</a>
 */
object SELinuxCompat {
    private val seLinuxClass by lazyReflectedClass("android.os.SELinux")
    private val isSELinuxEnabledMethod by lazyReflectedMethod(seLinuxClass, "isSELinuxEnabled")
    private val isSELinuxEnforcedMethod by lazyReflectedMethod(seLinuxClass, "isSELinuxEnforced")
    @RestrictedHiddenApi
    private val setFSCreateContextMethod by lazyReflectedMethod(
        seLinuxClass, "setFSCreateContext", String::class.java
    )
    @RestrictedHiddenApi
    private val setFileContextMethod by lazyReflectedMethod(
        seLinuxClass, "setFileContext", String::class.java, String::class.java
    )
    private val getFileContextStringMethod by lazyReflectedMethod(
        seLinuxClass, "getFileContext", String::class.java
    )
    @RestrictedHiddenApi
    private val getPeerContextMethod by lazyReflectedMethod(
        seLinuxClass, "getPeerContext", FileDescriptor::class.java
    )
    @get:RequiresApi(Build.VERSION_CODES.Q)
    @RestrictedHiddenApi
    private val getFileContextFileDescriptorMethod by lazyReflectedMethod(
        seLinuxClass, "getFileContext", FileDescriptor::class.java
    )
    private val getContextMethod by lazyReflectedMethod(seLinuxClass, "getContext")
    private val getPidContextMethod by lazyReflectedMethod(
        seLinuxClass, "getPidContext", Int::class.java
    )
    private val checkSELinuxAccessMethod by lazyReflectedMethod(
        seLinuxClass, "checkSELinuxAccess", String::class.java, String::class.java,
        String::class.java, String::class.java
    )
    @RestrictedHiddenApi
    private val nativeRestoreconMethod by lazyReflectedMethod(
        seLinuxClass, "native_restorecon", String::class.java, Int::class.java
    )
    @RestrictedHiddenApi
    private val restoreconStringMethod by lazyReflectedMethod(
        seLinuxClass, "restorecon", String::class.java
    )
    @RestrictedHiddenApi
    private val restoreconFileMethod by lazyReflectedMethod(
        seLinuxClass, "restorecon", File::class.java
    )
    private val restoreconRecursiveMethod by lazyReflectedMethod(
        seLinuxClass, "restoreconRecursive", File::class.java
    )

    val isSELinuxEnabled: Boolean
        get() = isSELinuxEnabledMethod.invoke(null) as Boolean

    val isSELinuxEnforced: Boolean
        get() = isSELinuxEnforcedMethod.invoke(null) as Boolean

    fun setFSCreateContext(context: String?): Boolean =
        setFSCreateContextMethod.invoke(null, context) as Boolean

    fun setFileContext(path: String, context: String): Boolean =
        setFileContextMethod.invoke(null, path, context) as Boolean

    fun getFileContext(path: String): String? =
        getFileContextStringMethod.invoke(null, path) as String?

    fun getPeerContext(fd: FileDescriptor): String? =
        getPeerContextMethod.invoke(null, fd) as String?

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getFileContext(fd: FileDescriptor): String? =
        getFileContextFileDescriptorMethod.invoke(null, fd) as String?

    val context: String?
        get() = getContextMethod.invoke(null) as String?

    fun getPidContext(pid: Int): String? = getPidContextMethod.invoke(null, pid) as String?

    fun checkSELinuxAccess(scon: String, tcon: String, tclass: String, perm: String): Boolean =
        checkSELinuxAccessMethod.invoke(null, scon, tcon, tclass, perm) as Boolean

    fun native_restorecon(pathname: String?, flags: Int): Boolean =
        nativeRestoreconMethod.invoke(null, pathname, flags) as Boolean

    fun restorecon(pathname: String): Boolean =
        restoreconStringMethod.invoke(null, pathname) as Boolean

    fun restorecon(file: File): Boolean = restoreconFileMethod.invoke(null, file) as Boolean

    fun restoreconRecursive(file: File): Boolean =
        restoreconRecursiveMethod.invoke(null, file) as Boolean
}
