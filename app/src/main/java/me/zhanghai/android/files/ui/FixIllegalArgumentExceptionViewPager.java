/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

// @see https://github.com/chrisbanes/PhotoView/blob/master/sample/src/main/java/com/github/chrisbanes/photoview/sample/HackyViewPager.java
public class FixIllegalArgumentExceptionViewPager extends ViewPager {

    public FixIllegalArgumentExceptionViewPager(@NonNull Context context) {
        super(context);
    }

    public FixIllegalArgumentExceptionViewPager(@NonNull Context context,
                                                @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull MotionEvent event) {
        try {
            return super.onInterceptTouchEvent(event);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }
}
