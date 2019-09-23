/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindDimen;
import butterknife.ButterKnife;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.util.ViewUtils;

public class NavigationRecyclerView extends RecyclerView {

    @BindDimen(R.dimen.design_navigation_elevation)
    float mElevation;
    @BindDimen(R.dimen.design_navigation_max_width)
    int mMaxWidth;
    @BindDimen(R.dimen.design_navigation_padding_bottom)
    int mVerticalPadding;
    private int mActionBarSize;
    private Drawable mScrim;

    private int mInsetTop;

    public NavigationRecyclerView(Context context) {
        super(context);

        init();
    }

    public NavigationRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public NavigationRecyclerView(Context context, AttributeSet attrs,
                                  int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        ButterKnife.bind(this);
        Context context = getContext();
        mActionBarSize = ViewUtils.getDimensionPixelSizeFromAttrRes(R.attr.actionBarSize, 0,
                context);
        mScrim = ViewUtils.getDrawableFromAttrRes(R.attr.colorSystemWindowScrim, context);
        setPadding(getPaddingLeft(), mVerticalPadding, getPaddingRight(), mVerticalPadding);
        setElevation(mElevation);
        setFitsSystemWindows(true);
        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int maxWidth = MathUtils.clamp(ViewUtils.getDisplayWidth(getContext()) - mActionBarSize, 0,
                mMaxWidth);
        switch (MeasureSpec.getMode(widthSpec)) {
            case MeasureSpec.AT_MOST:
                maxWidth = Math.min(maxWidth, MeasureSpec.getSize(widthSpec));
                // Fall through!
            case MeasureSpec.UNSPECIFIED:
                widthSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY);
                break;
        }
        super.onMeasure(widthSpec, heightSpec);
    }

    @NonNull
    @Override
    public WindowInsets onApplyWindowInsets(@NonNull WindowInsets insets) {
        mInsetTop = insets.getSystemWindowInsetTop();
        setPadding(getPaddingLeft(), mVerticalPadding + mInsetTop, getPaddingRight(),
                mVerticalPadding + insets.getSystemWindowInsetBottom());
        return insets.replaceSystemWindowInsets(insets.getSystemWindowInsetLeft(), 0,
                insets.getSystemWindowInsetRight(), 0);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        int saveCount = canvas.save();
        canvas.translate(getScrollX(), getScrollY());
        mScrim.setBounds(0, 0, getWidth(), mInsetTop);
        mScrim.draw(canvas);
        canvas.restoreToCount(saveCount);
    }
}
