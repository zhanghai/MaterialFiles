/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.reflected;

import java.lang.reflect.Method;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

abstract class BaseReflectedMethod<T> extends BaseReflectedExecutable<T> {

    @NonNull
    private final String mMethodName;

    @Nullable
    private Method mMethod;
    @NonNull
    private final Object mMethodLock = new Object();

    public BaseReflectedMethod(@NonNull String methodName, @NonNull Object... parameterTypes) {
        super(parameterTypes);

        mMethodName = methodName;
    }

    @NonNull
    public Method get() throws ReflectedException {
        synchronized (mMethodLock) {
            if (mMethod == null) {
                Class<?> ownerClass = getOwnerClass();
                Class<?>[] parameterTypes = getParameterTypes();
                mMethod = ReflectedAccessor.getAccessibleMethod(ownerClass, mMethodName,
                        parameterTypes);
            }
            return mMethod;
        }
    }

    public <R> R invoke(@Nullable T object, @NonNull Object... arguments)
            throws ReflectedException {
        return ReflectedAccessor.invoke(get(), object, arguments);
    }
}
