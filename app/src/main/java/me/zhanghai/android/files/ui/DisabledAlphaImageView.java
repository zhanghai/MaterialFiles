/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.content.Context;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import me.zhanghai.android.files.util.ViewUtils;

public class DisabledAlphaImageView extends AppCompatImageView {

    public DisabledAlphaImageView(@NonNull Context context) {
        super(context);
    }

    public DisabledAlphaImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DisabledAlphaImageView(@NonNull Context context, @Nullable AttributeSet attrs,
                                  @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);

        updateImageAlpha();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        updateImageAlpha();
    }

    private void updateImageAlpha() {
        int alpha = 0xFF;
        Drawable drawable = getDrawable();
        // AdaptiveIconDrawable might be stateful without respecting enabled state.
        boolean isAdaptiveIconDrawable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && drawable instanceof AdaptiveIconDrawable;
        if (drawable == null || isAdaptiveIconDrawable || !drawable.isStateful()) {
            boolean enabled = false;
            for (int state : getDrawableState()) {
                if (state == android.R.attr.state_enabled) {
                    enabled = true;
                }
            }
            if (!enabled) {
                float disabledAlpha = ViewUtils.getFloatFromAttrRes(android.R.attr.disabledAlpha, 0,
                        getContext());
                alpha = Math.round(disabledAlpha * alpha);
            }
        }
        setImageAlpha(alpha);
    }
}
