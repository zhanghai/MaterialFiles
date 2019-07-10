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
import androidx.appcompat.widget.AppCompatImageButton;

@SuppressLint("MissingSuperCall")
public class ForegroundImageButton extends AppCompatImageButton {

    private final ForegroundHelper mForegroundHelper = new ForegroundHelper(
            new ForegroundHelper.Delegate() {

                @NonNull
                @Override
                public View getView() {
                    return ForegroundImageButton.this;
                }

                @Override
                @RequiresApi(Build.VERSION_CODES.N)
                public void superOnVisibilityAggregated(boolean isVisible) {
                    ForegroundImageButton.super.onVisibilityAggregated(isVisible);
                }

                @Override
                public void superDraw(@NonNull Canvas canvas) {
                    ForegroundImageButton.super.draw(canvas);
                }

                @Override
                public void superOnRtlPropertiesChanged(int layoutDirection) {
                    ForegroundImageButton.super.onRtlPropertiesChanged(layoutDirection);
                }

                @Override
                public boolean superVerifyDrawable(@NonNull Drawable who) {
                    return ForegroundImageButton.super.verifyDrawable(who);
                }

                @Override
                public void superDrawableStateChanged() {
                    ForegroundImageButton.super.drawableStateChanged();
                }

                @Override
                public void superDrawableHotspotChanged(float x, float y) {
                    ForegroundImageButton.super.drawableHotspotChanged(x, y);
                }

                @Override
                public void superJumpDrawablesToCurrentState() {
                    ForegroundImageButton.super.jumpDrawablesToCurrentState();
                }

                @Nullable
                @Override
                @RequiresApi(Build.VERSION_CODES.M)
                public Drawable superGetForeground() {
                    return ForegroundImageButton.super.getForeground();
                }

                @Override
                @RequiresApi(Build.VERSION_CODES.M)
                public void superSetForeground(@Nullable Drawable foreground) {
                    ForegroundImageButton.super.setForeground(foreground);
                }

                @Override
                @RequiresApi(Build.VERSION_CODES.M)
                public int superGetForegroundGravity() {
                    return ForegroundImageButton.super.getForegroundGravity();
                }

                @Override
                @RequiresApi(Build.VERSION_CODES.M)
                public void superSetForegroundGravity(int gravity) {
                    ForegroundImageButton.super.setForegroundGravity(gravity);
                }

                @Override
                @RequiresApi(Build.VERSION_CODES.M)
                public void superSetForegroundTintList(@Nullable ColorStateList tint) {
                    ForegroundImageButton.super.setForegroundTintList(tint);
                }

                @Nullable
                @Override
                @RequiresApi(Build.VERSION_CODES.M)
                public ColorStateList superGetForegroundTintList() {
                    return ForegroundImageButton.super.getForegroundTintList();
                }

                @Override
                @RequiresApi(Build.VERSION_CODES.M)
                public void superSetForegroundTintMode(@Nullable PorterDuff.Mode tintMode) {
                    ForegroundImageButton.super.setForegroundTintMode(tintMode);
                }

                @Nullable
                @Override
                @RequiresApi(Build.VERSION_CODES.M)
                public PorterDuff.Mode superGetForegroundTintMode() {
                    return ForegroundImageButton.super.getForegroundTintMode();
                }
            });

    public ForegroundImageButton(@NonNull Context context) {
        super(context);

        mForegroundHelper.init(context, null, 0, 0);
    }

    public ForegroundImageButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mForegroundHelper.init(context, attrs, 0, 0);
    }

    public ForegroundImageButton(@NonNull Context context, @Nullable AttributeSet attrs,
                                 @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mForegroundHelper.init(context, attrs, defStyleAttr, 0);
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
