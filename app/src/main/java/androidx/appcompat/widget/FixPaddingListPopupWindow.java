/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package androidx.appcompat.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

public class FixPaddingListPopupWindow extends ListPopupWindow {
    public FixPaddingListPopupWindow(@NonNull Context context) {
        super(context);
    }

    public FixPaddingListPopupWindow(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FixPaddingListPopupWindow(@NonNull Context context, @Nullable AttributeSet attrs,
                                     @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FixPaddingListPopupWindow(@NonNull Context context, @Nullable AttributeSet attrs,
                                     @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @NonNull
    @Override
    DropDownListView createDropDownListView(@NonNull Context context, boolean hijackFocus) {
        return new FixPaddingDropDownListView(context, hijackFocus);
    }

    private static class FixPaddingDropDownListView extends DropDownListView {
        public FixPaddingDropDownListView(Context context, boolean hijackFocus) {
            super(context, hijackFocus);
        }

        // DropDownListView.measureHeightOfChildrenCompat() uses list padding instead of regular
        // padding, which isn't initialized before onMeasure() so returns no padding for the first
        // time. And ListPopupWindow.buildDropDown() adds the regular padding back every time, which
        // will double the padding after the first show.
        @Override
        public int measureHeightOfChildrenCompat(int widthMeasureSpec, int startPosition,
                                                 int endPosition, int maxHeight,
                                                 int disallowPartialChildPosition) {
            int height = super.measureHeightOfChildrenCompat(widthMeasureSpec, startPosition,
                    endPosition, maxHeight, disallowPartialChildPosition);
            height -= getListPaddingTop() + getListPaddingBottom();
            return height;
        }
    }
}
