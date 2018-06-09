/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.ui;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;

import me.zhanghai.android.materialfilemanager.util.ViewUtils;

/**
 * TextView that automatically sets its visibility to View.GONE when empty.
 */
public class AutoGoneTextView extends AppCompatTextView {

    public AutoGoneTextView(Context context) {
        super(context);
    }

    public AutoGoneTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoGoneTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);

        ViewUtils.setVisibleOrGone(this, !TextUtils.isEmpty(text));
    }
}
