/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import me.zhanghai.android.files.util.ViewUtils;

/**
 * TextView that automatically sets its visibility to View.GONE when empty.
 */
public class AutoGoneTextView extends AppCompatTextView {

    public AutoGoneTextView(@NonNull Context context) {
        super(context);
    }

    public AutoGoneTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoGoneTextView(@NonNull Context context, @Nullable AttributeSet attrs,
                            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setText(@Nullable CharSequence text, @NonNull BufferType type) {
        super.setText(text, type);

        ViewUtils.setVisibleOrGone(this, !TextUtils.isEmpty(text));
    }
}
