/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
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
    public Field get() {
        synchronized (mFieldLock) {
            if (mField == null) {
                Class<?> ownerClass = getOwnerClass();
                mField = ReflectedAccessor.getAccessibleField(ownerClass, mFieldName);
            }
            return mField;
        }
    }

    public <T> T getObject(@Nullable Object object) {
        return ReflectedAccessor.getObject(get(), object);
    }

    public boolean getBoolean(@Nullable Object object) {
        return ReflectedAccessor.getBoolean(get(), object);
    }

    public byte getByte(@Nullable Object object) {
        return ReflectedAccessor.getByte(get(), object);
    }

    public char getChar(@Nullable Object object) {
        return ReflectedAccessor.getChar(get(), object);
    }

    public short getShort(@Nullable Object object) {
        return ReflectedAccessor.getShort(get(), object);
    }

    public int getInt(@Nullable Object object) {
        return ReflectedAccessor.getInt(get(), object);
    }

    public long getLong(@Nullable Object object) {
        return ReflectedAccessor.getLong(get(), object);
    }

    public float getFloat(@Nullable Object object) {
        return ReflectedAccessor.getFloat(get(), object);
    }

    public double getDouble(@Nullable Object object) {
        return ReflectedAccessor.getDouble(get(), object);
    }

    public void setObject(@Nullable Object object, Object value) {
        ReflectedAccessor.setObject(get(), object, value);
    }

    public void setBoolean(@Nullable Object object, boolean value) {
        ReflectedAccessor.setBoolean(get(), object, value);
    }

    public void setByte(@Nullable Object object, byte value) {
        ReflectedAccessor.setByte(get(), object, value);
    }

    public void setChar(@Nullable Object object, char value) {
        ReflectedAccessor.setChar(get(), object, value);
    }

    public void setShort(@Nullable Object object, short value) {
        ReflectedAccessor.setShort(get(), object, value);
    }

    public void setInt(@Nullable Object object, int value) {
        ReflectedAccessor.setInt(get(), object, value);
    }

    public void setLong(@Nullable Object object, long value) {
        ReflectedAccessor.setLong(get(), object, value);
    }

    public void setFloat(@Nullable Object object, float value) {
        ReflectedAccessor.setFloat(get(), object, value);
    }

    public void setDouble(@Nullable Object object, double value) {
        ReflectedAccessor.setDouble(get(), object, value);
    }
}
