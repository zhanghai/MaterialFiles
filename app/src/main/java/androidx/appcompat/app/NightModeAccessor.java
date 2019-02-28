/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package androidx.appcompat.app;

import androidx.annotation.NonNull;

public class NightModeAccessor {

    private NightModeAccessor() {}

    public static int mapNightMode(@NonNull AppCompatDelegate delegate, int mode) {
        return ((AppCompatDelegateImpl) delegate).mapNightMode(mode);
    }
}
