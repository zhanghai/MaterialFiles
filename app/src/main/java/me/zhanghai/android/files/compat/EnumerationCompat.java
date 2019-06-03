/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import java.util.Enumeration;
import java.util.Iterator;

import androidx.annotation.NonNull;

public class EnumerationCompat {

    private EnumerationCompat() {}

    @NonNull
    public static <E> Iterator<E> asIterator(@NonNull Enumeration<E> enumeration) {
        return new Iterator<E>() {
            @Override
            public boolean hasNext() {
                return enumeration.hasMoreElements();
            }
            @Override
            public E next() {
                return enumeration.nextElement();
            }
        };
    }
}
