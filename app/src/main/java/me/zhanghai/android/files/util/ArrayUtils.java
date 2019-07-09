/*
 * Copyright (c) 2016 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ArrayUtils {

    public static int indexOf(@NonNull byte[] array, byte value) {
        for (int i = 0; i < array.length; ++i) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOf(@NonNull char[] array, char value) {
        for (int i = 0; i < array.length; ++i) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOf(@NonNull short[] array, short value) {
        for (int i = 0; i < array.length; ++i) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOf(@NonNull int[] array, int value) {
        for (int i = 0; i < array.length; ++i) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOf(@NonNull long[] array, long value) {
        for (int i = 0; i < array.length; ++i) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOf(@NonNull float[] array, float value) {
        for (int i = 0; i < array.length; ++i) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOf(@NonNull double[] array, double value) {
        for (int i = 0; i < array.length; ++i) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    public static <T> int indexOf(@NonNull T[] array, T value) {
        for (int i = 0; i < array.length; ++i) {
            if (Objects.equals(array[i], value)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isEmpty(@Nullable byte[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(@Nullable char[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(@Nullable short[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(@Nullable int[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(@Nullable long[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(@Nullable float[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(@Nullable double[] array) {
        return array == null || array.length == 0;
    }

    public static <T> boolean isEmpty(@Nullable T[] array) {
        return array == null || array.length == 0;
    }
}
