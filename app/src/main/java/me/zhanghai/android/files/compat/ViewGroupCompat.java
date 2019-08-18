/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import android.graphics.PointF;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.java.reflected.ReflectedMethod;

public class ViewGroupCompat {

    static {
        RestrictedHiddenApiAccess.allow();
    }

    @RestrictedHiddenApi
    private static final ReflectedMethod<ViewGroup> sIsTransformedTouchPointInViewMethod =
            new ReflectedMethod<>(ViewGroup.class, "isTransformedTouchPointInView", float.class,
                    float.class, View.class, PointF.class);

    private ViewGroupCompat() {}

    public static boolean isTransformedTouchPointInView(@NonNull ViewGroup viewGroup, float x,
                                                        float y, @NonNull View child,
                                                        @Nullable PointF outLocalPoint) {
        return sIsTransformedTouchPointInViewMethod.invoke(viewGroup, x, y, child, outLocalPoint);
    }
}
