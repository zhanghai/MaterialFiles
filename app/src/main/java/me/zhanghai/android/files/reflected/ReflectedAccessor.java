/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.reflected;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ReflectedAccessor {

    @NonNull
    static <T> Class<T> getClass(@NonNull String className) throws ReflectedException {
        try {
            //noinspection unchecked
            return (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ReflectedException(e);
        }
    }

    @NonNull
    static <T> Constructor<T> getAccessibleConstructor(@NonNull Class<T> declaringClass,
                                                       @NonNull Class<?>... parameterTypes)
            throws ReflectedException {
        try {
            Constructor<T> constructor = declaringClass.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            throw new ReflectedException(e);
        }
    }

    @NonNull
    static Field getAccessibleField(@NonNull Class<?> declaringClass, @NonNull String fieldName)
            throws ReflectedException {
        try {
            Field field = declaringClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new ReflectedException(e);
        }
    }

    @NonNull
    static Method getAccessibleMethod(@NonNull Class<?> declaringClass, @NonNull String methodName,
                                      @NonNull Class<?>... parameterTypes)
            throws ReflectedException {
        try {
            Method method = declaringClass.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            throw new ReflectedException(e);
        }
    }

    @NonNull
    static <T> T newInstance(@NonNull Constructor<T> constructor, @NonNull Object... arguments)
            throws ReflectedException {
        //noinspection TryWithIdenticalCatches
        try {
            return constructor.newInstance(arguments);
        } catch (IllegalAccessException e) {
            throw new ReflectedException(e);
        } catch (InstantiationException e) {
            throw new ReflectedException(e);
        } catch (InvocationTargetException e) {
            throw new ReflectedException(e);
        }
    }

    static <T> T getObject(@NonNull Field field, @Nullable Object object)
            throws ReflectedException {
        try {
            //noinspection unchecked
            return (T) field.get(object);
        } catch (IllegalAccessException e) {
            throw new ReflectedException(e);
        }
    }

    static boolean getBoolean(@NonNull Field field, @Nullable Object object)
            throws ReflectedException {
        try {
            return field.getBoolean(object);
        } catch (IllegalAccessException e) {
            throw new ReflectedException(e);
        }
    }

    static byte getByte(@NonNull Field field, @Nullable Object object) throws ReflectedException {
        try {
            return field.getByte(object);
        } catch (IllegalAccessException e) {
            throw new ReflectedException(e);
        }
    }

    static char getChar(@NonNull Field field, @Nullable Object object) throws ReflectedException {
        try {
            return field.getChar(object);
        } catch (IllegalAccessException e) {
            throw new ReflectedException(e);
        }
    }

    static short getShort(@NonNull Field field, @Nullable Object object) throws ReflectedException {
        try {
            return field.getShort(object);
        } catch (IllegalAccessException e) {
            throw new ReflectedException(e);
        }
    }

    static int getInt(@NonNull Field field, @Nullable Object object) throws ReflectedException {
        try {
            return field.getInt(object);
        } catch (IllegalAccessException e) {
            throw new ReflectedException(e);
        }
    }

    static long getLong(@NonNull Field field, @Nullable Object object) throws ReflectedException {
        try {
            return field.getLong(object);
        } catch (IllegalAccessException e) {
            throw new ReflectedException(e);
        }
    }

    static float getFloat(@NonNull Field field, @Nullable Object object) throws ReflectedException {
        try {
            return field.getFloat(object);
        } catch (IllegalAccessException e) {
            throw new ReflectedException(e);
        }
    }

    static double getDouble(@NonNull Field field, @Nullable Object object)
            throws ReflectedException {
        try {
            return field.getDouble(object);
        } catch (IllegalAccessException e) {
            throw new ReflectedException(e);
        }
    }

    static void setObject(@NonNull Field field, @Nullable Object object, Object value)
            throws ReflectedException {
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw new ReflectedException(e);
        }
    }

    static void setBoolean(@NonNull Field field, @Nullable Object object, boolean value)
            throws ReflectedException {
        try {
            field.setBoolean(object, value);
        } catch (IllegalAccessException e) {
            throw new ReflectedException(e);
        }
    }

    static void setByte(@NonNull Field field, @Nullable Object object, byte value)
            throws ReflectedException {
        try {
            field.setByte(object, value);
        } catch (IllegalAccessException e) {
            throw new ReflectedException(e);
        }
    }

    static void setChar(@NonNull Field field, @Nullable Object object, char value)
            throws ReflectedException {
        try {
            field.setChar(object, value);
        } catch (IllegalAccessException e) {
            throw new ReflectedException(e);
        }
    }

    static void setShort(@NonNull Field field, @Nullable Object object, short value)
            throws ReflectedException {
        try {
            field.setShort(object, value);
        } catch (IllegalAccessException e) {
            throw new ReflectedException(e);
        }
    }

    static void setInt(@NonNull Field field, @Nullable Object object, int value)
            throws ReflectedException {
        try {
            field.setInt(object, value);
        } catch (IllegalAccessException e) {
            throw new ReflectedException(e);
        }
    }

    static void setLong(@NonNull Field field, @Nullable Object object, long value)
            throws ReflectedException {
        try {
            field.setLong(object, value);
        } catch (IllegalAccessException e) {
            throw new ReflectedException(e);
        }
    }

    static void setFloat(@NonNull Field field, @Nullable Object object, float value)
            throws ReflectedException {
        try {
            field.setFloat(object, value);
        } catch (IllegalAccessException e) {
            throw new ReflectedException(e);
        }
    }

    static void setDouble(@NonNull Field field, @Nullable Object object, double value)
            throws ReflectedException {
        try {
            field.setDouble(object, value);
        } catch (IllegalAccessException e) {
            throw new ReflectedException(e);
        }
    }

    static <T> T invoke(@NonNull Method method, @Nullable Object object,
                        @NonNull Object... arguments) throws ReflectedException {
        //noinspection TryWithIdenticalCatches
        try {
            //noinspection unchecked
            return (T) method.invoke(object, arguments);
        } catch (IllegalAccessException e) {
            throw new ReflectedException(e);
        } catch (InvocationTargetException e) {
            throw new ReflectedException(e);
        }
    }
}
