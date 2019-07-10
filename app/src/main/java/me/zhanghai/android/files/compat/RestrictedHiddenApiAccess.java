/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import android.os.Build;

import java.lang.reflect.Method;

import androidx.annotation.NonNull;
import me.zhanghai.java.reflected.ReflectedAccessor;
import me.zhanghai.java.reflected.ReflectedMethod;

/*
 * @see http://weishu.me/2019/03/16/another-free-reflection-above-android-p/
 * @see https://github.com/tiann/FreeReflection/blob/master/library/src/main/java/me/weishu/reflection/Reflection.java
 */
public class RestrictedHiddenApiAccess {

    @NonNull
    private static final ReflectedMethod<Class> sClassForNameMethod = new ReflectedMethod<>(
            Class.class, "forName", String.class);
    @NonNull
    private static final ReflectedMethod<Class> sClassGetDeclaredMethodMethod =
            new ReflectedMethod<>(Class.class, "getDeclaredMethod", String.class, Class[].class);

    private static boolean sAllowed;
    @NonNull
    private static final Object sAllowedLock = new Object();

    public static void allow() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return;
        }
        synchronized (sAllowedLock) {
            if (!sAllowed) {
                Class<?> vmRuntimeClass = sClassForNameMethod.invoke(null,
                        "dalvik.system.VMRuntime");
                Method vmRuntimeGetRuntimeMethod = sClassGetDeclaredMethodMethod.invoke(
                        vmRuntimeClass, "getRuntime", new Class[0]);
                Object vmRuntime = ReflectedAccessor.invoke(vmRuntimeGetRuntimeMethod, null);
                Method vmRuntimeSetHiddenApiExemptionsMethod = sClassGetDeclaredMethodMethod.invoke(
                        vmRuntimeClass, "setHiddenApiExemptions", new Class[] { String[].class });
                ReflectedAccessor.invoke(vmRuntimeSetHiddenApiExemptionsMethod, vmRuntime,
                        (Object) new String[] { "" });
                sAllowed = true;
            }
        }
    }
}
