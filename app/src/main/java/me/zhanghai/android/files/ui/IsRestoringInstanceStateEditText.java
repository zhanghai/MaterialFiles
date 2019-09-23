/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

public class IsRestoringInstanceStateEditText extends AppCompatEditText {

    private boolean mRestoringInstanceState;

    public IsRestoringInstanceStateEditText(@NonNull Context context) {
        super(context);
    }

    public IsRestoringInstanceStateEditText(@NonNull Context context,
                                            @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public IsRestoringInstanceStateEditText(@NonNull Context context, @Nullable AttributeSet attrs,
                                            @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Parcelable state) {
        mRestoringInstanceState = true;
        super.onRestoreInstanceState(state);
        mRestoringInstanceState = false;
    }

    public boolean isRestoringInstanceState() {
        return mRestoringInstanceState;
    }
}
