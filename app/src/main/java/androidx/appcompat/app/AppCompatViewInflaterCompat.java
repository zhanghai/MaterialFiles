/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package androidx.appcompat.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AppCompatViewInflaterCompat {
    private AppCompatViewInflaterCompat() {}

    @Nullable
    public static View createView(@NonNull AppCompatViewInflater appCompatViewInflater,
                                  @Nullable View parent, @NonNull String name,
                                  @NonNull Context context, @NonNull AttributeSet attrs,
                                  boolean inheritContext, boolean readAndroidTheme,
                                  boolean readAppTheme, boolean wrapContext) {
        return appCompatViewInflater.createView(parent, name, context, attrs, inheritContext,
                readAndroidTheme, readAppTheme, wrapContext);
    }
}
