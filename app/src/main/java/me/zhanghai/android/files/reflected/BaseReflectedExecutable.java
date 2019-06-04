/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.reflected;

import androidx.annotation.NonNull;

abstract class BaseReflectedExecutable<T> {

    @NonNull
    private final Object[] mParameterTypes;

    public BaseReflectedExecutable(@NonNull Object... parameterTypes) {
        mParameterTypes = checkParameterTypes(parameterTypes);
    }

    @NonNull
    private static Object[] checkParameterTypes(@NonNull Object[] parameterTypes) {
        Object[] newParameterTypes = new Object[parameterTypes.length];
        for (int i = 0, count = parameterTypes.length; i < count; ++i) {
            Object parameterType = parameterTypes[i];
            if (parameterType instanceof String) {
                newParameterTypes[i] = new ReflectedClass((String) parameterType);
            } else if (parameterType instanceof Class<?>
                    || parameterType instanceof ReflectedClass) {
                newParameterTypes[i] = parameterType;
            } else {
                throw new IllegalArgumentException("Parameter type must be a class name, a Class or"
                        + " a ReflectedClass: " + parameterType);
            }
        }
        return newParameterTypes;
    }

    @NonNull
    protected abstract Class<T> getOwnerClass();

    @NonNull
    protected Class<?>[] getParameterTypes() {
        Class<?>[] parameterTypes = new Class<?>[mParameterTypes.length];
        for (int i = 0, count = mParameterTypes.length; i < count; ++i) {
            Object parameterType = mParameterTypes[i];
            if (parameterType instanceof Class<?>) {
                parameterTypes[i] = (Class<?>) parameterType;
            } else if (parameterType instanceof ReflectedClass) {
                parameterTypes[i] = ((ReflectedClass) parameterType).get();
            } else {
                throw new IllegalArgumentException();
            }
        }
        return parameterTypes;
    }
}
