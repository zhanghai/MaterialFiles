/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package androidx.appcompat.app;

public class NightModeAccessor {

    private NightModeAccessor() {}

    public static int mapNightMode(AppCompatDelegate delegate, int mode) {
        return ((AppCompatDelegateImpl) delegate).mapNightMode(mode);
    }
}
