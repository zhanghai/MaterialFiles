/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import butterknife.BindDrawable;
import butterknife.ButterKnife;
import me.zhanghai.android.materialfilemanager.R;

public class MaxDimensionScrimPaddingRecyclerView extends RecyclerView {

    @BindDrawable(R.color.system_window_scrim)
    Drawable mScrimDrawable;

    private MaxDimensionHelper mMaxDimensionHelper = new MaxDimensionHelper((widthSpec, heightSpec)
            -> MaxDimensionScrimPaddingRecyclerView.super.onMeasure(widthSpec, heightSpec));

    public MaxDimensionScrimPaddingRecyclerView(Context context) {
        super(context);

        init(null, 0, 0);
    }

    public MaxDimensionScrimPaddingRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(attrs, 0, 0);
    }

    public MaxDimensionScrimPaddingRecyclerView(Context context, AttributeSet attrs,
                                                int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(attrs, defStyleAttr, 0);
    }

    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        ButterKnife.bind(this);
        mMaxDimensionHelper.init(getContext(), attrs, defStyleAttr, defStyleRes);
        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        mMaxDimensionHelper.onMeasure(widthSpec, heightSpec);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        int width = getWidth();
        int height = getHeight();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int saveCount = canvas.save();
        canvas.translate((float) getScrollX(), (float) getScrollY());
        mScrimDrawable.setBounds(0, 0, width, paddingTop);
        mScrimDrawable.draw(canvas);
        mScrimDrawable.setBounds(0, height - paddingBottom, width, height);
        mScrimDrawable.draw(canvas);
        mScrimDrawable.setBounds(0, paddingTop, paddingLeft, height - paddingBottom);
        mScrimDrawable.draw(canvas);
        mScrimDrawable.setBounds(width - paddingRight, paddingTop, width,
                height - paddingBottom);
        mScrimDrawable.draw(canvas);
        canvas.restoreToCount(saveCount);
    }
}
