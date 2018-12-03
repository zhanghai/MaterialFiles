/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.reflected;

import android.os.Build;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ReflectedAccessor {

    @NonNull
    private static final ReflectedField sClassClassLoaderField = new ReflectedField(Class.class,
            "classLoader");

    private static boolean sRestrictedHiddenApiAccessAllowed;
    @NonNull
    private static final Object sRestrictedHiddenApiAccessAllowedLock = new Object();

    public static void allowRestrictedHiddenApiAccess() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return;
        }
        synchronized (sRestrictedHiddenApiAccessAllowedLock) {
            if (!sRestrictedHiddenApiAccessAllowed) {
                sClassClassLoaderField.setObject(ReflectedAccessor.class, null);
                sRestrictedHiddenApiAccessAllowed = true;
            }
        }
    }

    @NonNull
    public static Class<?> getClass(@NonNull String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    public static Field getAccessibleField(@NonNull Class<?> ownerClass,
                                           @NonNull String fieldName) {
        try {
            Field field = ownerClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    public static Method getAccessibleMethod(@NonNull Class<?> ownerClass,
                                             @NonNull String methodName,
                                             @NonNull Class<?>... parameterTypes) {
        try {
            Method method = ownerClass.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getObject(@NonNull Field field, @Nullable Object object) {
        try {
            //noinspection unchecked
            return (T) field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean getBoolean(@NonNull Field field, @Nullable Object object) {
        try {
            return field.getBoolean(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte getByte(@NonNull Field field, @Nullable Object object) {
        try {
            return field.getByte(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static char getChar(@NonNull Field field, @Nullable Object object) {
        try {
            return field.getChar(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static short getShort(@NonNull Field field, @Nullable Object object) {
        try {
            return field.getShort(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getInt(@NonNull Field field, @Nullable Object object) {
        try {
            return field.getInt(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static long getLong(@NonNull Field field, @Nullable Object object) {
        try {
            return field.getLong(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static float getFloat(@NonNull Field field, @Nullable Object object) {
        try {
            return field.getFloat(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static double getDouble(@NonNull Field field, @Nullable Object object) {
        try {
            return field.getDouble(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setObject(@NonNull Field field, @Nullable Object object, Object value) {
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setBoolean(@NonNull Field field, @Nullable Object object, boolean value) {
        try {
            field.setBoolean(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setByte(@NonNull Field field, @Nullable Object object, byte value) {
        try {
            field.setByte(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setChar(@NonNull Field field, @Nullable Object object, char value) {
        try {
            field.setChar(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setShort(@NonNull Field field, @Nullable Object object, short value) {
        try {
            field.setShort(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setInt(@NonNull Field field, @Nullable Object object, int value) {
        try {
            field.setInt(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setLong(@NonNull Field field, @Nullable Object object, long value) {
        try {
            field.setLong(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setFloat(@NonNull Field field, @Nullable Object object, float value) {
        try {
            field.setFloat(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setDouble(@NonNull Field field, @Nullable Object object, double value) {
        try {
            field.setDouble(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T invoke(@NonNull Method method, @Nullable Object object,
                               @NonNull Object... arguments) {
        //noinspection TryWithIdenticalCatches
        try {
            //noinspection unchecked
            return (T) method.invoke(object, arguments);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
