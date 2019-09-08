/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.text.method.MovementMethod;
import android.util.AttributeSet;

import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.util.ViewUtils;

public class TextInputTextView extends TextInputEditText {

    public TextInputTextView(@NonNull Context context) {
        super(context);

        init();
    }

    public TextInputTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public TextInputTextView(@NonNull Context context, @Nullable AttributeSet attrs,
                             @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        setClickable(false);
        setFocusable(false);
    }

    @Override
    public boolean getFreezesText() {
        return false;
    }

    @Override
    protected boolean getDefaultEditable() {
        return false;
    }

    @Nullable
    @Override
    protected MovementMethod getDefaultMovementMethod() {
        return null;
    }

    @Override
    public void setBackgroundDrawable(@Nullable Drawable background) {
        if (background instanceof MaterialShapeDrawable) {
            background = addRippleEffect((MaterialShapeDrawable) background);
        }
        super.setBackgroundDrawable(background);
    }

    // @see com.google.android.material.textfield.DropdownMenuEndIconDelegate#addRippleEffect(
    //      AutoCompleteTextView)
    @NonNull
    private Drawable addRippleEffect(@NonNull MaterialShapeDrawable boxBackground) {
        ColorStateList rippleColor = ViewUtils.getColorStateListFromAttrRes(
                R.attr.colorControlHighlight, getContext());
        MaterialShapeDrawable mask = new MaterialShapeDrawable(
                boxBackground.getShapeAppearanceModel());
        mask.setTint(Color.WHITE);
        return new RippleDrawable(rippleColor, boxBackground, mask);
    }
}
