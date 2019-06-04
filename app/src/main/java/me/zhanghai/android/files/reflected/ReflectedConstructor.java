/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.reflected;

import androidx.annotation.NonNull;

import java.lang.reflect.Constructor;

public class ReflectedConstructor<T> extends ReflectedExecutable<T, Constructor<T>> {

    public ReflectedConstructor(@NonNull Class<T> declaringClass,
                                @NonNull Object... parameterTypes) {
        super(declaringClass, parameterTypes);
    }

    public ReflectedConstructor(@NonNull ReflectedClass<T> declaringClass,
                                @NonNull Object... parameterTypes) {
        super(declaringClass, parameterTypes);
    }

    public ReflectedConstructor(@NonNull String declaringClassName,
                                @NonNull Object... parameterTypes) {
        super(declaringClassName, parameterTypes);
    }

    @NonNull
    @Override
    protected Constructor<T> onGet() throws ReflectedException {
        return ReflectedAccessor.getAccessibleConstructor(getDeclaringClass(), getParameterTypes());
    }

    @NonNull
    public T newInstance(@NonNull Object... arguments) throws ReflectedException {
        return ReflectedAccessor.newInstance(get(), arguments);
    }
}
