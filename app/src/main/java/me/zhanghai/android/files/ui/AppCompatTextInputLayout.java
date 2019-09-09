/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.TintTypedArray;
import androidx.core.widget.TextViewCompat;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.util.ViewUtils;

public class AppCompatTextInputLayout extends TextInputLayout {

    public AppCompatTextInputLayout(@NonNull Context context) {
        super(context);

        init();
    }

    public AppCompatTextInputLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public AppCompatTextInputLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                                    @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        setHintAnimationEnabled(false);
        setDefaultHintTextColor(ViewUtils.getColorStateListFromAttrRes(
                android.R.attr.textColorSecondary, getContext()));
    }

    @Override
    public void addView(@NonNull View child, int index, @NonNull ViewGroup.LayoutParams params) {
        if (child instanceof EditText) {
            EditText editText = (EditText) child;
            Context context = getContext();
            int verticalPadding = ViewUtils.dpToPxOffset(8, context);
            if (editText.isTextSelectable()) {
                editText.setBackground(null);
                editText.setPadding(0, verticalPadding, 0, verticalPadding);
            } else {
                int spinnerStyleRes = ViewUtils.getResIdFromAttrRes(R.attr.spinnerStyle, 0,
                        context);
                TintTypedArray a = TintTypedArray.obtainStyledAttributes(getContext(),
                        spinnerStyleRes, new int[] { android.R.attr.background });
                Drawable spinnerBackground = a.getDrawable(0);
                a.recycle();
                editText.setBackground(spinnerBackground);
                ((MarginLayoutParams) params).setMarginEnd(ViewUtils.dpToPxOffset(-19, context));
                editText.setPadding(editText.getPaddingLeft(), verticalPadding,
                        editText.getPaddingRight(), verticalPadding);
            }
            TextViewCompat.setTextAppearance(editText, R.style.TextAppearance_AppCompat_Subhead);
        }
        super.addView(child, index, params);
    }
}
