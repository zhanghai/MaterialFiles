/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
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
