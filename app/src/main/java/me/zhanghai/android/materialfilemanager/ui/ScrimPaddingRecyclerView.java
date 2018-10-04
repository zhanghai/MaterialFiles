/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import butterknife.BindDrawable;
import butterknife.ButterKnife;
import me.zhanghai.android.materialfilemanager.R;

public class ScrimPaddingRecyclerView extends RecyclerView {

    @BindDrawable(R.color.system_window_scrim)
    Drawable mScrimDrawable;

    public ScrimPaddingRecyclerView(@NonNull Context context) {
        super(context);

        init();
    }

    public ScrimPaddingRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public ScrimPaddingRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs,
                                    int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {
        ButterKnife.bind(this);
        setWillNotDraw(false);
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
