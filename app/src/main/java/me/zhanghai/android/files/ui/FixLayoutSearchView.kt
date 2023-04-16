/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes
import androidx.appcompat.widget.SearchView
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePaddingRelative
import me.zhanghai.android.files.compat.requireViewByIdCompat
import me.zhanghai.android.files.util.dpToDimensionPixelSize
import me.zhanghai.android.files.util.getDrawableByAttr

open class FixLayoutSearchView : SearchView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    init {
        // A negative value won't work here because SearchView will use its preferred width as max
        // width instead.
        maxWidth = Int.MAX_VALUE
        val searchEditFrame = requireViewByIdCompat<View>(androidx.appcompat.R.id.search_edit_frame)
        searchEditFrame.updateLayoutParams<MarginLayoutParams> {
            leftMargin = 0
            rightMargin = 0
        }
        val searchSrcText = requireViewByIdCompat<View>(androidx.appcompat.R.id.search_src_text)
        searchSrcText.updatePaddingRelative(start = 0, end = 0)
        val searchCloseBtn = requireViewByIdCompat<View>(androidx.appcompat.R.id.search_close_btn)
        val searchCloseBtnPaddingHorizontal = searchCloseBtn.context.dpToDimensionPixelSize(12)
        searchCloseBtn.updatePaddingRelative(
            start = searchCloseBtnPaddingHorizontal, end = searchCloseBtnPaddingHorizontal
        )
        searchCloseBtn.background = searchCloseBtn.context
            .getDrawableByAttr(androidx.appcompat.R.attr.actionBarItemBackground)
    }
}
