/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.reflected;

import java.lang.reflect.Method;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ReflectedMethod<T> extends ReflectedExecutable<T, Method> {

    @NonNull
    private final String mMethodName;

    public ReflectedMethod(@NonNull Class<T> declaringClass, @NonNull String methodName,
                           @NonNull Object... parameterTypes) {
        super(declaringClass, parameterTypes);

        mMethodName = Objects.requireNonNull(methodName);
    }

    public ReflectedMethod(@NonNull ReflectedClass<T> declaringClass, @NonNull String methodName,
                           @NonNull Object... parameterTypes) {
        super(declaringClass, parameterTypes);

        mMethodName = Objects.requireNonNull(methodName);
    }

    public ReflectedMethod(@NonNull String declaringClassName, @NonNull String methodName,
                           @NonNull Object... parameterTypes) {
        super(declaringClassName, parameterTypes);

        mMethodName = Objects.requireNonNull(methodName);
    }

    @NonNull
    @Override
    protected Method onGet() throws ReflectedException {
        return ReflectedAccessor.getAccessibleMethod(getDeclaringClass(), mMethodName,
                getParameterTypes());
    }

    public <R> R invoke(@Nullable T object, @NonNull Object... arguments)
            throws ReflectedException {
        return ReflectedAccessor.invoke(get(), object, arguments);
    }
}
