/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewParent;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java9.util.function.Consumer;

public class SyncAppBarElevationToolbar extends MaterialToolbar {

    public SyncAppBarElevationToolbar(@NonNull Context context) {
        super(context);
    }

    public SyncAppBarElevationToolbar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SyncAppBarElevationToolbar(@NonNull Context context, @Nullable AttributeSet attrs,
                                      @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        AppBarLayout appBarLayout = null;
        ViewParent parent = getParent();
        while (parent != null) {
            if (parent instanceof AppBarLayout) {
                appBarLayout = (AppBarLayout) parent;
                break;
            }
            parent = parent.getParent();
        }
        if (appBarLayout != null) {
            Drawable appBarLayoutBackground = appBarLayout.getBackground();
            if (!(appBarLayoutBackground instanceof SetElevationCallbackDrawable)
                    && appBarLayoutBackground instanceof MaterialShapeDrawable) {
                appBarLayoutBackground = new SetElevationCallbackDrawable(
                        (MaterialShapeDrawable) appBarLayoutBackground, elevation ->
                        MaterialShapeUtils.setElevation(this, elevation), getContext());
                appBarLayout.setBackground(appBarLayoutBackground);
            }
        }
    }

    private static class SetElevationCallbackDrawable extends MaterialShapeDrawable {

        @NonNull
        private final Consumer<Float> mSetElevationCallback;

        public SetElevationCallbackDrawable(@NonNull MaterialShapeDrawable drawable,
                                            @NonNull Consumer<Float> setElevationCallback,
                                            @NonNull Context context) {
            mSetElevationCallback = setElevationCallback;
            setFillColor(drawable.getFillColor());
            initializeElevationOverlay(context);
        }

        @Override
        public void setElevation(float elevation) {
            super.setElevation(elevation);

            mSetElevationCallback.accept(elevation);
        }
    }
}
