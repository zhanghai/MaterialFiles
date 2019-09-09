/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.foregroundcompat;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StyleRes;

public class ForegroundLinearLayout extends LinearLayout implements ForegroundCompatView {

    private final ForegroundHelper mForegroundHelper = new ForegroundHelper(this);

    public ForegroundLinearLayout(@NonNull Context context) {
        super(context);

        mForegroundHelper.init(context, null, 0, 0);
    }

    public ForegroundLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mForegroundHelper.init(context, attrs, 0, 0);
    }

    public ForegroundLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                                  @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mForegroundHelper.init(context, attrs, defStyleAttr, 0);
    }

    public ForegroundLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                                  @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mForegroundHelper.init(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    @RequiresApi(Build.VERSION_CODES.N)
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);

        mForegroundHelper.onVisibilityAggregated(isVisible);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        mForegroundHelper.draw(canvas);
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);

        mForegroundHelper.onRtlPropertiesChanged(layoutDirection);
    }

    @Override
    public boolean verifyDrawable(@NonNull Drawable who) {
        return super.verifyDrawable(who) || mForegroundHelper.verifyDrawable(who);
    }

    @Override
    public void drawableStateChanged() {
        super.drawableStateChanged();

        mForegroundHelper.drawableStateChanged();
    }

    @Override
    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);

        mForegroundHelper.drawableHotspotChanged(x, y);
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();

        mForegroundHelper.jumpDrawablesToCurrentState();
    }

    @Nullable
    @Override
    public Drawable getSupportForeground() {
        return mForegroundHelper.getSupportForeground();
    }

    @Override
    public void setSupportForeground(@Nullable Drawable foreground) {
        mForegroundHelper.setSupportForeground(foreground);
    }

    @Override
    public int getSupportForegroundGravity() {
        return mForegroundHelper.getSupportForegroundGravity();
    }

    @Override
    public void setSupportForegroundGravity(int gravity) {
        mForegroundHelper.setSupportForegroundGravity(gravity);
    }

    @Override
    public void setSupportForegroundTintList(@Nullable ColorStateList tint) {
        mForegroundHelper.setSupportForegroundTintList(tint);
    }

    @Nullable
    @Override
    public ColorStateList getSupportForegroundTintList() {
        return mForegroundHelper.getSupportForegroundTintList();
    }

    @Override
    public void setSupportForegroundTintMode(@Nullable PorterDuff.Mode tintMode) {
        mForegroundHelper.setSupportForegroundTintMode(tintMode);
    }

    @Nullable
    @Override
    public PorterDuff.Mode getSupportForegroundTintMode() {
        return mForegroundHelper.getSupportForegroundTintMode();
    }
}
