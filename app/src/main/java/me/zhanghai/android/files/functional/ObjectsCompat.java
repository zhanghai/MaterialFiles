/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.functional;

import java.util.Objects;

public class ObjectsCompat {

    private ObjectsCompat() {}

    /**
     * @see Objects#isNull
     */
    public static boolean isNull(Object object) {
        return object == null;
    }

    /**
     * @see Objects#nonNull
     */
    public static boolean nonNull(Object object) {
        return object != null;
    }
}
