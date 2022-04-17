/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package androidx.swiperefreshlayout.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.compat.ContextCompatKt;
import me.zhanghai.android.files.util.ContextExtensionsKt;

public class ThemedSwipeRefreshLayout extends SwipeRefreshLayout {
    public ThemedSwipeRefreshLayout(@NonNull Context context) {
        super(context);

        init();
    }

    public ThemedSwipeRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        Context context = getContext();
        boolean isMaterial3Theme = ContextExtensionsKt.isMaterial3Theme(context);
        int backgroundColor;
        if (isMaterial3Theme) {
            int surfaceColor = ContextExtensionsKt.getColorByAttr(context,
                    R.attr.colorSurface);
            @SuppressLint("PrivateResource")
            int overlayColor = ContextCompatKt.getColorCompat(context,
                    R.color.m3_popupmenu_overlay_color);
            backgroundColor = ColorUtils.compositeColors(overlayColor, surfaceColor);
        } else {
            backgroundColor = ContextExtensionsKt.getColorByAttr(context,
                    R.attr.colorBackgroundFloating);
        }
        ((ShapeDrawable) mCircleView.getBackground()).getPaint().setColor(backgroundColor);
        setColorSchemeColors(ContextExtensionsKt.getColorByAttr(context, R.attr.colorAccent));
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        View child = getChildView();
        if (child != null) {
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            setMeasuredDimension(child.getMeasuredWidth() + getPaddingLeft() + getPaddingRight(),
                    child.getMeasuredHeight() + getPaddingTop() + getPaddingBottom());
        }
    }

    @Nullable
    private View getChildView() {
        for (int i = 0; i < getChildCount(); ++i) {
            View child = getChildAt(i);
            if (!child.equals(mCircleView)) {
                return child;
            }
        }
        return null;
    }
}
