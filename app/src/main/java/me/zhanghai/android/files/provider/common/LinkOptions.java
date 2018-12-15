/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.util.Objects;

import androidx.annotation.NonNull;
import java8.nio.file.LinkOption;

public class LinkOptions {

    private LinkOptions() {}

    public static boolean hasNoFollowLinks(@NonNull LinkOption... options) {
        boolean noFollowLinks = false;
        for (LinkOption option : options) {
            Objects.requireNonNull(option);
            if (option == LinkOption.NOFOLLOW_LINKS) {
                noFollowLinks = true;
            } else {
                throw new UnsupportedOperationException(option.toString());
            }
        }
        return noFollowLinks;
    }
}
