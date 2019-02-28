/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.reflected;

import java.lang.reflect.Field;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

abstract class BaseReflectedField {

    @NonNull
    private final String mFieldName;

    @Nullable
    private Field mField;
    @NonNull
    private final Object mFieldLock = new Object();

    public BaseReflectedField(@NonNull String fieldName) {
        mFieldName = fieldName;
    }

    @NonNull
    protected abstract Class<?> getOwnerClass();

    @NonNull
    public Field get() throws ReflectedException {
        synchronized (mFieldLock) {
            if (mField == null) {
                Class<?> ownerClass = getOwnerClass();
                mField = ReflectedAccessor.getAccessibleField(ownerClass, mFieldName);
            }
            return mField;
        }
    }

    public <T> T getObject(@Nullable Object object) throws ReflectedException {
        return ReflectedAccessor.getObject(get(), object);
    }

    public boolean getBoolean(@Nullable Object object) throws ReflectedException {
        return ReflectedAccessor.getBoolean(get(), object);
    }

    public byte getByte(@Nullable Object object) throws ReflectedException {
        return ReflectedAccessor.getByte(get(), object);
    }

    public char getChar(@Nullable Object object) throws ReflectedException {
        return ReflectedAccessor.getChar(get(), object);
    }

    public short getShort(@Nullable Object object) throws ReflectedException {
        return ReflectedAccessor.getShort(get(), object);
    }

    public int getInt(@Nullable Object object) throws ReflectedException {
        return ReflectedAccessor.getInt(get(), object);
    }

    public long getLong(@Nullable Object object) throws ReflectedException {
        return ReflectedAccessor.getLong(get(), object);
    }

    public float getFloat(@Nullable Object object) throws ReflectedException {
        return ReflectedAccessor.getFloat(get(), object);
    }

    public double getDouble(@Nullable Object object) throws ReflectedException {
        return ReflectedAccessor.getDouble(get(), object);
    }

    public void setObject(@Nullable Object object, Object value) throws ReflectedException {
        ReflectedAccessor.setObject(get(), object, value);
    }

    public void setBoolean(@Nullable Object object, boolean value) throws ReflectedException {
        ReflectedAccessor.setBoolean(get(), object, value);
    }

    public void setByte(@Nullable Object object, byte value) throws ReflectedException {
        ReflectedAccessor.setByte(get(), object, value);
    }

    public void setChar(@Nullable Object object, char value) throws ReflectedException {
        ReflectedAccessor.setChar(get(), object, value);
    }

    public void setShort(@Nullable Object object, short value) throws ReflectedException {
        ReflectedAccessor.setShort(get(), object, value);
    }

    public void setInt(@Nullable Object object, int value) throws ReflectedException {
        ReflectedAccessor.setInt(get(), object, value);
    }

    public void setLong(@Nullable Object object, long value) throws ReflectedException {
        ReflectedAccessor.setLong(get(), object, value);
    }

    public void setFloat(@Nullable Object object, float value) throws ReflectedException {
        ReflectedAccessor.setFloat(get(), object, value);
    }

    public void setDouble(@Nullable Object object, double value) throws ReflectedException {
        ReflectedAccessor.setDouble(get(), object, value);
    }
}
