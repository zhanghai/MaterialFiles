/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package android.support.v4.widget;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.util.ViewUtils;

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
        ((ShapeDrawable) mCircleView.getBackground()).getPaint().setColor(
                ViewUtils.getColorFromAttrRes(R.attr.colorBackgroundFloating, 0, context));
        setColorSchemeColors(ViewUtils.getColorFromAttrRes(R.attr.colorAccent, 0, context));
    }
}
