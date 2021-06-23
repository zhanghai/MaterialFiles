/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.os.Build
import me.zhanghai.android.files.java.ArrayOfClasses
import me.zhanghai.android.files.util.lazyReflectedMethod
import java.lang.reflect.Method

/*
 * @see http://weishu.me/2019/03/16/another-free-reflection-above-android-p/
 * @see https://github.com/tiann/FreeReflection/blob/master/library/src/main/java/me/weishu/reflection/Reflection.java
 */
object RestrictedHiddenApiAccess {
    private val classForNameMethod by lazyReflectedMethod(
        Class::class.java, "forName", String::class.java
    )
    private val classGetDeclaredMethodMethod by lazyReflectedMethod(
        Class::class.java, "getDeclaredMethod", String::class.java, ArrayOfClasses.CLASS
    )

    private var allowed = false
    private val allowedLock = Any()

    fun allow() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return
        }
        synchronized(allowedLock) {
            if (!allowed) {
                val vmRuntimeClass = classForNameMethod.invoke(
                    null, "dalvik.system.VMRuntime"
                ) as Class<*>
                val vmRuntimeGetRuntimeMethod = classGetDeclaredMethodMethod.invoke(
                    vmRuntimeClass, "getRuntime", arrayOfNulls<Class<*>>(0)
                ) as Method
                val vmRuntime = vmRuntimeGetRuntimeMethod.invoke(null)
                val vmRuntimeSetHiddenApiExemptionsMethod =
                    classGetDeclaredMethodMethod.invoke(
                        vmRuntimeClass, "setHiddenApiExemptions",
                        arrayOf<Class<*>>(Array<String>::class.java)
                    ) as Method
                vmRuntimeSetHiddenApiExemptionsMethod.invoke(vmRuntime, arrayOf(""))
                allowed = true
            }
        }
    }
}
