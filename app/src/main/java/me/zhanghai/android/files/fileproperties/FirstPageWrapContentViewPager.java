/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class FirstPageWrapContentViewPager extends ViewPager {

    public FirstPageWrapContentViewPager(@NonNull Context context) {
        super(context);
    }

    public FirstPageWrapContentViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY && getChildCount() > 0) {
            View child = getChildAt(0);
            child.measure(widthMeasureSpec, heightMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(),
                    MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
