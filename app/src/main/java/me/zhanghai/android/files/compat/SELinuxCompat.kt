/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.os.Build
import androidx.annotation.RequiresApi
import me.zhanghai.java.reflected.ReflectedClass
import me.zhanghai.java.reflected.ReflectedMethod
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
    private val seLinuxClass = ReflectedClass<Any>("android.os.SELinux")
    private val isSELinuxEnabledMethod = ReflectedMethod(seLinuxClass, "isSELinuxEnabled")
    private val isSELinuxEnforcedMethod = ReflectedMethod(seLinuxClass, "isSELinuxEnforced")
    @RestrictedHiddenApi
    private val setFSCreateContextMethod =
        ReflectedMethod(seLinuxClass, "setFSCreateContext", String::class.java)
    @RestrictedHiddenApi
    private val setFileContextMethod =
        ReflectedMethod(seLinuxClass, "setFileContext", String::class.java, String::class.java)
    private val getFileContextStringMethod =
        ReflectedMethod(seLinuxClass, "getFileContext", String::class.java)
    @RestrictedHiddenApi
    private val getPeerContextMethod =
        ReflectedMethod(seLinuxClass, "getPeerContext", FileDescriptor::class.java)
    @RequiresApi(Build.VERSION_CODES.Q)
    @RestrictedHiddenApi
    private val getFileContextFileDescriptorMethod =
        ReflectedMethod(seLinuxClass, "getFileContext", FileDescriptor::class.java)
    private val getContextMethod = ReflectedMethod(seLinuxClass, "getContext")
    private val getPidContextMethod =
        ReflectedMethod(seLinuxClass, "getPidContext", Int::class.java)
    private val checkSELinuxAccessMethod = ReflectedMethod(
        seLinuxClass, "checkSELinuxAccess", String::class.java, String::class.java,
        String::class.java, String::class.java
    )
    @RestrictedHiddenApi
    private val nativeRestoreconMethod =
        ReflectedMethod(seLinuxClass, "native_restorecon", String::class.java, Int::class.java)
    @RestrictedHiddenApi
    private val restoreconStringMethod =
        ReflectedMethod(seLinuxClass, "restorecon", String::class.java)
    @RestrictedHiddenApi
    private val restoreconFileMethod =
        ReflectedMethod(seLinuxClass, "restorecon", File::class.java)
    private val restoreconRecursiveMethod =
        ReflectedMethod(seLinuxClass, "restoreconRecursive", File::class.java)

    val isSELinuxEnabled: Boolean
        get() = isSELinuxEnabledMethod.invoke(null)

    val isSELinuxEnforced: Boolean
        get() = isSELinuxEnforcedMethod.invoke(null)

    fun setFSCreateContext(context: String?): Boolean =
        setFSCreateContextMethod.invoke(null, context)

    fun setFileContext(path: String, context: String): Boolean =
        setFileContextMethod.invoke(null, path, context)

    fun getFileContext(path: String): String? = getFileContextStringMethod.invoke(null, path)

    fun getPeerContext(fd: FileDescriptor): String? = getPeerContextMethod.invoke(null, fd)

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getFileContext(fd: FileDescriptor): String? =
        getFileContextFileDescriptorMethod.invoke(null, fd)

    val context: String?
        get() = getContextMethod.invoke(null)

    fun getPidContext(pid: Int): String? = getPidContextMethod.invoke(null, pid)

    fun checkSELinuxAccess(scon: String, tcon: String, tclass: String, perm: String): Boolean =
        checkSELinuxAccessMethod.invoke(null, scon, tcon, tclass, perm)

    fun native_restorecon(pathname: String?, flags: Int): Boolean =
        nativeRestoreconMethod.invoke(null, pathname, flags)

    fun restorecon(pathname: String): Boolean = restoreconStringMethod.invoke(null, pathname)

    fun restorecon(file: File): Boolean = restoreconFileMethod.invoke(null, file)

    fun restoreconRecursive(file: File): Boolean = restoreconRecursiveMethod.invoke(null, file)
}
