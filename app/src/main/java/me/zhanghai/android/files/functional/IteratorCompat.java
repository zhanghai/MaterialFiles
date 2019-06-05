/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.functional;

import java.util.Iterator;

import java9.util.function.Consumer;

public class IteratorCompat {

    private IteratorCompat() {}

    public static <T> void forEachRemaining(Iterator<T> iterator, Consumer<T> consumer) {
        while (iterator.hasNext()) {
            T t = iterator.next();
            consumer.accept(t);
        }
    }
}
