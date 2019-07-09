/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.colorpicker;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.ui.CheckableView;

public class ColorSwatchView extends CheckableView {

    private GradientDrawable mGradientDrawable;

    public ColorSwatchView(@NonNull Context context) {
        super(context);

        init();
    }

    public ColorSwatchView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public ColorSwatchView(@NonNull Context context, @Nullable AttributeSet attrs,
                           @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        LayerDrawable drawable = (LayerDrawable) AppCompatResources.getDrawable(getContext(),
                R.drawable.color_swatch_view_background);
        mGradientDrawable = (GradientDrawable) drawable.getDrawable(0);
        setBackground(drawable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }

    public void setColor(@ColorInt int color) {
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(color);
    }
}
