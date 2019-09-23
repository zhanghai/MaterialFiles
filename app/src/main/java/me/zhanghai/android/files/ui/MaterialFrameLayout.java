/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

public class MaterialFrameLayout extends FrameLayout {

    public MaterialFrameLayout(@NonNull Context context) {
        super(context);

        init();
    }

    public MaterialFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public MaterialFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                               @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public MaterialFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                               @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        Drawable backgroundDrawable = getBackground();
        if (backgroundDrawable instanceof ColorDrawable) {
            ColorDrawable backgroundColorDrawable = (ColorDrawable) backgroundDrawable;
            MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();
            materialShapeDrawable.setFillColor(ColorStateList.valueOf(
                    backgroundColorDrawable.getColor()));
            materialShapeDrawable.initializeElevationOverlay(getContext());
            materialShapeDrawable.setElevation(getElevation());
            setBackground(materialShapeDrawable);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        MaterialShapeUtils.setParentAbsoluteElevation(this);
    }

    @Override
    public void setElevation(float elevation) {
        super.setElevation(elevation);

        MaterialShapeUtils.setElevation(this, elevation);
    }
}
