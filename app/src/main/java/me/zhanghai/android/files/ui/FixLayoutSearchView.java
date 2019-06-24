/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.ViewCompat;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.util.ViewUtils;

public class FixLayoutSearchView extends SearchView {

    public FixLayoutSearchView(@NonNull Context context) {
        super(context);

        init();
    }

    public FixLayoutSearchView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public FixLayoutSearchView(@NonNull Context context, @Nullable AttributeSet attrs,
                               int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {

        View searchEditFrame = ViewCompat.requireViewById(this, R.id.search_edit_frame);
        MarginLayoutParams searchEditFrameLayoutParams = (MarginLayoutParams)
                searchEditFrame.getLayoutParams();
        searchEditFrameLayoutParams.leftMargin = 0;
        searchEditFrameLayoutParams.rightMargin = 0;
        searchEditFrame.setLayoutParams(searchEditFrameLayoutParams);

        View searchSrcText = ViewCompat.requireViewById(this, R.id.search_src_text);
        searchSrcText.setPaddingRelative(0, searchSrcText.getPaddingTop(), 0,
                searchSrcText.getPaddingBottom());

        View searchCloseBtn = ViewCompat.requireViewById(this, R.id.search_close_btn);
        int searchCloseBtnPaddingHorizontal = ViewUtils.dpToPxOffset(12,
                searchCloseBtn.getContext());
        searchCloseBtn.setPaddingRelative(searchCloseBtnPaddingHorizontal,
                searchCloseBtn.getPaddingTop(), searchCloseBtnPaddingHorizontal,
                searchCloseBtn.getPaddingBottom());
        searchCloseBtn.setBackground(ViewUtils.getDrawableFromAttrRes(
                R.attr.actionBarItemBackground, searchCloseBtn.getContext()));
    }
}
