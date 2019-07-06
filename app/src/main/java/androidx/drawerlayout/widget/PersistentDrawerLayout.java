/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package androidx.drawerlayout.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PersistentDrawerLayout extends DrawerLayout {

    public PersistentDrawerLayout(@NonNull Context context) {
        super(context);

        init();
    }

    public PersistentDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public PersistentDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                                  @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        setDrawerElevation(0);
        setScrimColor(Color.TRANSPARENT);
        addDrawerListener(new SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                requestLayout();
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // TODO: Or LOCK_MODE_LOCKED_CLOSED
        setDrawerLockMode(LOCK_MODE_LOCKED_OPEN);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int contentWidth = width;
        int contentHeight = MeasureSpec.getSize(heightMeasureSpec);
        for (int i = 0, count = getChildCount(); i < count; ++i) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            if (!isContentView(child)) {
                LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
                contentWidth -= (int) (child.getMeasuredWidth() * childLayoutParams.onScreen)
                        + childLayoutParams.leftMargin + childLayoutParams.rightMargin;
            }
        }
        if (contentWidth == width) {
            return;
        }
        for (int i = 0, count = getChildCount(); i < count; ++i) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            if (isContentView(child)) {
                LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
                int contentWidthSpec = MeasureSpec.makeMeasureSpec(contentWidth
                                - childLayoutParams.leftMargin - childLayoutParams.rightMargin,
                        MeasureSpec.EXACTLY);
                int contentHeightSpec = MeasureSpec.makeMeasureSpec(contentHeight
                                - childLayoutParams.topMargin - childLayoutParams.bottomMargin,
                        MeasureSpec.EXACTLY);
                child.measure(contentWidthSpec, contentHeightSpec);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int contentLeft = 0;
        for (int i = 0, count = getChildCount(); i < count; ++i) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            if (!isContentView(child)) {
                if (checkDrawerViewAbsoluteGravity(child, Gravity.LEFT)) {
                    LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
                    contentLeft = child.getRight() + childLayoutParams.rightMargin;
                }
            }
        }
        if (contentLeft == 0) {
            return;
        }
        for (int i = 0, count = getChildCount(); i < count; ++i) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            if (isContentView(child)) {
                LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
                int childLeft = contentLeft + childLayoutParams.leftMargin;
                child.layout(childLeft, childLayoutParams.topMargin,
                        childLeft + child.getMeasuredWidth(),
                        childLayoutParams.topMargin + child.getMeasuredHeight());
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull MotionEvent event) {
        return false;
    }
}
