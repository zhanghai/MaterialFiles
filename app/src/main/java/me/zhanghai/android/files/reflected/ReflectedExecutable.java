/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.reflected;

import androidx.annotation.NonNull;

import java.util.Objects;

abstract class ReflectedExecutable<T, M> extends ReflectedMember<T, M> {

    @NonNull
    private final Object[] mParameterTypes;

    public ReflectedExecutable(@NonNull Class<T> declaringClass,
                               @NonNull Object... parameterTypes) {
        super(declaringClass);

        mParameterTypes = checkAndTransformParameterTypes(parameterTypes);
    }

    public ReflectedExecutable(@NonNull ReflectedClass<T> declaringClass,
                               @NonNull Object... parameterTypes) {
        super(declaringClass);

        mParameterTypes = checkAndTransformParameterTypes(parameterTypes);
    }

    public ReflectedExecutable(@NonNull String declaringClassName,
                               @NonNull Object... parameterTypes) {
        super(declaringClassName);

        mParameterTypes = checkAndTransformParameterTypes(parameterTypes);
    }

    @NonNull
    private static Object[] checkAndTransformParameterTypes(@NonNull Object[] parameterTypes) {
        Objects.requireNonNull(parameterTypes);
        Object[] transformedParameterTypes = new Object[parameterTypes.length];
        for (int i = 0, count = parameterTypes.length; i < count; ++i) {
            Object parameterType = parameterTypes[i];
            Objects.requireNonNull(parameterType);
            if (parameterType instanceof Class || parameterType instanceof ReflectedClass) {
                transformedParameterTypes[i] = parameterType;
            } else if (parameterType instanceof String) {
                transformedParameterTypes[i] = new ReflectedClass<>((String) parameterType);
            } else {
                throw new IllegalArgumentException("Parameter type must be a Class, a"
                        + " ReflectedClass or a class name: " + parameterType);
            }
        }
        return transformedParameterTypes;
    }

    @NonNull
    protected Class<?>[] getParameterTypes() {
        Class<?>[] parameterTypes = new Class<?>[mParameterTypes.length];
        for (int i = 0, count = mParameterTypes.length; i < count; ++i) {
            Object parameterType = mParameterTypes[i];
            if (parameterType instanceof Class) {
                parameterTypes[i] = (Class<?>) parameterType;
            } else if (parameterType instanceof ReflectedClass) {
                parameterTypes[i] = ((ReflectedClass<?>) parameterType).get();
            } else {
                throw new AssertionError(parameterType);
            }
        }
        return parameterTypes;
    }
}
