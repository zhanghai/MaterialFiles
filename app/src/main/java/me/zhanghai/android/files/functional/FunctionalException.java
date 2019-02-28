/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.functional;

public class FunctionalException extends RuntimeException {

    public FunctionalException(Throwable cause) {
        super(cause);
    }

    public <T extends Throwable> T getCauseAs(Class<T> classOfCause) {
        //noinspection unchecked
        return (T) super.getCause();
    }
}
