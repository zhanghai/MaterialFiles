/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.foreground;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StyleRes;

@SuppressLint("MissingSuperCall")
public class ForegroundView extends View {

    private final ForegroundHelper mForegroundHelper = new ForegroundHelper(
            new ForegroundHelper.Delegate() {

                @NonNull
                @Override
                public View getView() {
                    return ForegroundView.this;
                }

                @Override
                @RequiresApi(Build.VERSION_CODES.N)
                public void superOnVisibilityAggregated(boolean isVisible) {
                    ForegroundView.super.onVisibilityAggregated(isVisible);
                }

                @Override
                public void superDraw(@NonNull Canvas canvas) {
                    ForegroundView.super.draw(canvas);
                }

                @Override
                public void superOnRtlPropertiesChanged(int layoutDirection) {
                    ForegroundView.super.onRtlPropertiesChanged(layoutDirection);
                }

                @Override
                public boolean superVerifyDrawable(@NonNull Drawable who) {
                    return ForegroundView.super.verifyDrawable(who);
                }

                @Override
                public void superDrawableStateChanged() {
                    ForegroundView.super.drawableStateChanged();
                }

                @Override
                public void superDrawableHotspotChanged(float x, float y) {
                    ForegroundView.super.drawableHotspotChanged(x, y);
                }

                @Override
                public void superJumpDrawablesToCurrentState() {
                    ForegroundView.super.jumpDrawablesToCurrentState();
                }

                @Nullable
                @Override
                @RequiresApi(Build.VERSION_CODES.M)
                public Drawable superGetForeground() {
                    return ForegroundView.super.getForeground();
                }

                @Override
                @RequiresApi(Build.VERSION_CODES.M)
                public void superSetForeground(@Nullable Drawable foreground) {
                    ForegroundView.super.setForeground(foreground);
                }

                @Override
                @RequiresApi(Build.VERSION_CODES.M)
                public int superGetForegroundGravity() {
                    return ForegroundView.super.getForegroundGravity();
                }

                @Override
                @RequiresApi(Build.VERSION_CODES.M)
                public void superSetForegroundGravity(int gravity) {
                    ForegroundView.super.setForegroundGravity(gravity);
                }

                @Override
                @RequiresApi(Build.VERSION_CODES.M)
                public void superSetForegroundTintList(@Nullable ColorStateList tint) {
                    ForegroundView.super.setForegroundTintList(tint);
                }

                @Nullable
                @Override
                @RequiresApi(Build.VERSION_CODES.M)
                public ColorStateList superGetForegroundTintList() {
                    return ForegroundView.super.getForegroundTintList();
                }

                @Override
                @RequiresApi(Build.VERSION_CODES.M)
                public void superSetForegroundTintMode(@Nullable PorterDuff.Mode tintMode) {
                    ForegroundView.super.setForegroundTintMode(tintMode);
                }

                @Nullable
                @Override
                @RequiresApi(Build.VERSION_CODES.M)
                public PorterDuff.Mode superGetForegroundTintMode() {
                    return ForegroundView.super.getForegroundTintMode();
                }
            });

    public ForegroundView(@NonNull Context context) {
        super(context);

        mForegroundHelper.init(context, null, 0, 0);
    }

    public ForegroundView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mForegroundHelper.init(context, attrs, 0, 0);
    }

    public ForegroundView(@NonNull Context context, @Nullable AttributeSet attrs,
                          @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mForegroundHelper.init(context, attrs, defStyleAttr, 0);
    }

    public ForegroundView(@NonNull Context context, @Nullable AttributeSet attrs,
                          @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mForegroundHelper.init(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    @RequiresApi(Build.VERSION_CODES.N)
    public void onVisibilityAggregated(boolean isVisible) {
        mForegroundHelper.onVisibilityAggregated(isVisible);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        mForegroundHelper.draw(canvas);
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        mForegroundHelper.onRtlPropertiesChanged(layoutDirection);
    }

    @Override
    public boolean verifyDrawable(@NonNull Drawable who) {
        return mForegroundHelper.verifyDrawable(who);
    }

    @Override
    public void drawableStateChanged() {
        mForegroundHelper.drawableStateChanged();
    }

    @Override
    public void drawableHotspotChanged(float x, float y) {
        mForegroundHelper.drawableHotspotChanged(x, y);
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        mForegroundHelper.jumpDrawablesToCurrentState();
    }

    @Nullable
    @Override
    public Drawable getForeground() {
        return mForegroundHelper.getForeground();
    }

    @Override
    public void setForeground(@Nullable Drawable foreground) {
        mForegroundHelper.setForeground(foreground);
    }

    @Override
    public int getForegroundGravity() {
        return mForegroundHelper.getForegroundGravity();
    }

    @Override
    public void setForegroundGravity(int gravity) {
        mForegroundHelper.setForegroundGravity(gravity);
    }

    @Override
    public void setForegroundTintList(@Nullable ColorStateList tint) {
        mForegroundHelper.setForegroundTintList(tint);
    }

    @Nullable
    @Override
    public ColorStateList getForegroundTintList() {
        return mForegroundHelper.getForegroundTintList();
    }

    @Override
    public void setForegroundTintMode(@Nullable PorterDuff.Mode tintMode) {
        mForegroundHelper.setForegroundTintMode(tintMode);
    }

    @Nullable
    @Override
    public PorterDuff.Mode getForegroundTintMode() {
        return mForegroundHelper.getForegroundTintMode();
    }
}
