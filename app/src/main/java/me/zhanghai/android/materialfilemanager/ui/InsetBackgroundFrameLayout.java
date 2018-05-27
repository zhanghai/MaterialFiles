/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.TintTypedArray;
import android.util.AttributeSet;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import me.zhanghai.android.materialfilemanager.R;

/**
 * A {@link FrameLayout} that draws background for system window insets.
 */
public class InsetBackgroundFrameLayout extends FrameLayout {

    private Drawable mInsetBackground;

    private Rect mInsets;

    public InsetBackgroundFrameLayout(Context context) {
        super(context);

        init(null, 0, 0);
    }

    public InsetBackgroundFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(attrs, 0, 0);
    }

    public InsetBackgroundFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(attrs, defStyleAttr, 0);
    }

    public InsetBackgroundFrameLayout(Context context, AttributeSet attrs, int defStyleAttr,
                                      int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(attrs, defStyleAttr, defStyleRes);
    }

    @SuppressLint("RestrictedApi")
    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {

        TintTypedArray a = TintTypedArray.obtainStyledAttributes(getContext(), attrs,
                R.styleable.InsetBackgroundFrameLayout, defStyleAttr, defStyleRes);
        mInsetBackground = a.getDrawable(R.styleable.InsetBackgroundFrameLayout_insetBackground);
        a.recycle();

        // Will not draw until insets are set.
        setWillNotDraw(true);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {

        if (mInsets == null) {
            mInsets = new Rect();
        }
        mInsets.set(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());

        if (willNotDraw()) {
            setWillNotDraw(!mInsets.isEmpty() && mInsetBackground != null);
        }
        ViewCompat.postInvalidateOnAnimation(this);

        return super.onApplyWindowInsets(insets);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int count = canvas.save();
        canvas.translate(getScrollX(), getScrollY());

        int width = getWidth();
        int height = getHeight();

        if (mInsets != null && mInsetBackground != null) {

            // Top
            mInsetBackground.setBounds(0, 0, width, mInsets.top);
            mInsetBackground.draw(canvas);

            // Bottom
            mInsetBackground.setBounds(0, height - mInsets.bottom, width, height);
            mInsetBackground.draw(canvas);

            // Left
            mInsetBackground.setBounds(0, mInsets.top, mInsets.left, height - mInsets.bottom);
            mInsetBackground.draw(canvas);

            // Right
            mInsetBackground.setBounds(width - mInsets.right, mInsets.top, width,
                    height - mInsets.bottom);
            mInsetBackground.draw(canvas);
        }

        canvas.restoreToCount(count);

        super.onDraw(canvas);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mInsetBackground != null) {
            mInsetBackground.setCallback(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mInsetBackground != null) {
            mInsetBackground.setCallback(null);
        }
    }
}
