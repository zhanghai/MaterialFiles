/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.os.Build
import me.zhanghai.android.files.java.ArrayOfClasses
import me.zhanghai.java.reflected.ReflectedAccessor
import me.zhanghai.java.reflected.ReflectedMethod
import java.lang.reflect.Method

/*
 * @see http://weishu.me/2019/03/16/another-free-reflection-above-android-p/
 * @see https://github.com/tiann/FreeReflection/blob/master/library/src/main/java/me/weishu/reflection/Reflection.java
 */
object RestrictedHiddenApiAccess {
    private val classForNameMethod = ReflectedMethod(
        Class::class.java, "forName", String::class.java
    )
    private val classGetDeclaredMethodMethod = ReflectedMethod(
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
                val vmRuntimeClass = classForNameMethod.invoke<Class<*>>(
                    null, "dalvik.system.VMRuntime"
                )
                val vmRuntimeGetRuntimeMethod = classGetDeclaredMethodMethod.invoke<Method>(
                    vmRuntimeClass, "getRuntime", arrayOfNulls<Class<*>>(0)
                )
                val vmRuntime = ReflectedAccessor.invoke<Any>(vmRuntimeGetRuntimeMethod, null)
                val vmRuntimeSetHiddenApiExemptionsMethod =
                    classGetDeclaredMethodMethod.invoke<Method>(
                        vmRuntimeClass, "setHiddenApiExemptions",
                        arrayOf<Class<*>>(Array<String>::class.java)
                    )
                ReflectedAccessor.invoke<Any>(
                    // TODO: Just arrayOf("")? Kotlin has spread operator so it won't be ambiguous.
                    vmRuntimeSetHiddenApiExemptionsMethod, vmRuntime, arrayOf("") as Any
                )
                allowed = true
            }
        }
    }
}
