/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.reflected;

import java.lang.reflect.Method;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

abstract class BaseReflectedMethod {

    @NonNull
    private final String mMethodName;
    @NonNull
    private final Object[] mParameterTypes;

    @Nullable
    private Method mMethod;
    @NonNull
    private final Object mMethodLock = new Object();

    public BaseReflectedMethod(@NonNull String methodName, @NonNull Object... parameterTypes) {
        mMethodName = methodName;
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
    protected abstract Class<?> getOwnerClass();

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

    @NonNull
    private Class<?>[] getParameterTypes() {
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

    public <T> T invoke(@Nullable Object object, @NonNull Object... arguments)
            throws ReflectedException {
        return ReflectedAccessor.invoke(get(), object, arguments);
    }
}
