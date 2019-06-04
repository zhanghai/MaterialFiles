/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.reflected;

import java.lang.reflect.Constructor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

abstract class BaseReflectedConstructor<T> extends BaseReflectedExecutable<T> {

    @Nullable
    private Constructor<T> mConstructor;
    @NonNull
    private final Object mConstructorLock = new Object();

    BaseReflectedConstructor(@NonNull Object... parameterTypes) {
        super(parameterTypes);
    }

    @NonNull
    public Constructor<T> get() throws ReflectedException {
        synchronized (mConstructorLock) {
            if (mConstructor == null) {
                Class<T> ownerClass = getOwnerClass();
                Class<?>[] parameterTypes = getParameterTypes();
                mConstructor = ReflectedAccessor.getAccessibleConstructor(ownerClass,
                        parameterTypes);
            }
            return mConstructor;
        }
    }

    @NonNull
    public T newInstance(@NonNull Object... arguments) throws ReflectedException {
        return ReflectedAccessor.newInstance(get(), arguments);
    }
}
