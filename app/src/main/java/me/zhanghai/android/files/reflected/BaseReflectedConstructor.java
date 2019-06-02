/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.reflected;

import java.lang.reflect.Constructor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

abstract class BaseReflectedConstructor<T> {

    @NonNull
    private final Object[] mParameterTypes;

    @Nullable
    private Constructor<T> mConstructor;
    @NonNull
    private final Object mConstructorLock = new Object();

    public BaseReflectedConstructor(@NonNull Object... parameterTypes) {
        mParameterTypes = BaseReflectedMethod.checkParameterTypes(parameterTypes);
    }

    @NonNull
    protected abstract Class<T> getOwnerClass();

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
    private Class<?>[] getParameterTypes() {
        return BaseReflectedMethod.getParameterTypes(mParameterTypes);
    }

    public T newInstance(@NonNull Object... arguments) throws ReflectedException {
        return ReflectedAccessor.newInstance(get(), arguments);
    }
}
